package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.RoomType;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.FormatCheckService;
import dev.milan.jpasolopractice.service.PersonService;
import dev.milan.jpasolopractice.service.YogaSessionService;
import dev.milan.jpasolopractice.service.YogaSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class YogaSessionServiceTest {
    @Autowired
    private YogaSessionService sessionService;
    @MockBean
    private YogaSessionServiceImpl sessionServiceImpl;
    @MockBean
    private YogaSessionRepository yogaSessionRepository;
    @MockBean
    private PersonRepository personRepository;
    @MockBean
    private PersonService personService;
    @MockBean
    private FormatCheckService formatCheckService;

    private LocalDate date;
    private RoomType yogaRoomType;
    private LocalTime startTime;
    private int duration;
    private YogaSession session;
    private Person personOne;
    private final LocalDate today = LocalDate.now();
    private String dateString;
    private String roomTypeString;
    private String startTimeString;
    private String durationString;

    @BeforeEach
    void init(){
        date = today.plus(1, ChronoUnit.DAYS);

        Room roomOne = new Room();
        roomOne.setDate(today.plus(1,ChronoUnit.DAYS));
        roomOne.setOpeningHours(LocalTime.of(5,0,0));
        roomOne.setClosingHours(LocalTime.of(20,0,0));
        roomOne.setRoomType(RoomType.AIR_ROOM);
        roomOne.setTotalCapacity(RoomType.AIR_ROOM.getMaxCapacity());

        Room roomTwo = new Room();
        roomTwo.setRoomType(RoomType.EARTH_ROOM);
        roomTwo.setDate(today.plus(1,ChronoUnit.DAYS));
        roomTwo.setOpeningHours(LocalTime.of(9,0,0));
        roomTwo.setClosingHours(LocalTime.of(23,0,0));
        roomTwo.setTotalCapacity(RoomType.EARTH_ROOM.getMaxCapacity());

        startTime = LocalTime.of(10,0,0);
        duration = 60;

        session = new YogaSession();
        session.setRoom(roomOne);
        session.setStartOfSession(LocalTime.of(8,0,0));
        session.setDuration(45);
        session.setDate(today);

        personOne = new Person();
        personOne.setEmail("example@hotmail.com");
        personOne.setAge(33);
        personOne.setName("Badji");
        personOne.setName("Kukumber");

        yogaRoomType = RoomType.AIR_ROOM;

        dateString = date.toString();
        roomTypeString = yogaRoomType.name();
        startTimeString = startTime.toString();
        durationString = "" + duration;
    }

    @Nested
    class CreateYogaSession{
        @Test
        void should_createYogaSession_when_sessionInfoCorrect() throws NotFoundApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
            when(formatCheckService.checkDateFormat(any())).thenReturn(date);
            when(formatCheckService.checkNumberFormat(anyString())).thenReturn(duration);
            when(formatCheckService.checkRoomTypeFormat(any())).thenReturn(yogaRoomType);
            when(formatCheckService.checkTimeFormat(startTimeString)).thenReturn(startTime);
            when(sessionServiceImpl.createAYogaSession(date,yogaRoomType,startTime,duration)).thenReturn(session);

            assertEquals(session, sessionService.createAYogaSession(dateString, roomTypeString,startTimeString,durationString));
        }
        @Test
        void should_throwException400BadRequest_when_creatingSessionAndRoomIsNull() throws BadRequestApiRequestException {
            assertThrows(BadRequestApiRequestException.class,() ->  sessionService.createAYogaSession(dateString, null,startTimeString,durationString));
        }
        @Test
        void should_throwException400BadRequest_when_creatingSessionAndDateIsNull() throws BadRequestApiRequestException {
            assertThrows(BadRequestApiRequestException.class,() ->  sessionService.createAYogaSession(null, roomTypeString,startTimeString,durationString));
        }
        @Test
        void should_saveSessionInRepo_when_sessionInfoCorrect() throws NotFoundApiRequestException {
            ArgumentCaptor<YogaSession> sessionCaptor = ArgumentCaptor.forClass(YogaSession.class);
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
            when(sessionServiceImpl.createAYogaSession(any(),any(),any(),anyInt())).thenReturn(session);

            sessionService.createAYogaSession(dateString, roomTypeString,startTimeString,durationString);

            verify(yogaSessionRepository,times(1)).save(sessionCaptor.capture());
            assertEquals(session, sessionCaptor.getValue());
        }

        @Test
        void should_notSaveSession_when_sessionInfoIncorrect() {
            try{
                when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
                when(sessionServiceImpl.createAYogaSession(any(),any(),any(),anyInt())).thenThrow(new NotFoundApiRequestException(""));
                sessionService.createAYogaSession(dateString, roomTypeString,startTimeString,durationString);
            }catch (ApiRequestException e){

            }finally {
                verify(yogaSessionRepository,never()).save(any());
            }
        }

        @Test
        void should_throwException400BadRequest_When_SessionInfoIncorrect() throws BadRequestApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(any(),any(),any())).thenReturn(null);
            when(sessionServiceImpl.createAYogaSession(any(),any(),any(),anyInt())).thenThrow(new BadRequestApiRequestException(""));
            Executable executable = () -> sessionService.createAYogaSession(dateString,roomTypeString,startTimeString,durationString);
            assertThrows(BadRequestApiRequestException.class, executable);
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionDateIsNull() throws BadRequestApiRequestException {
            Exception exception = assertThrows(BadRequestApiRequestException.class, () -> sessionService.createAYogaSession(null,roomTypeString,startTimeString,durationString));
            assertEquals("Date, room type, start time and duration must have values assigned.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionRoomIsNull() throws BadRequestApiRequestException {
            Exception exception = assertThrows(BadRequestApiRequestException.class, () -> sessionService.createAYogaSession(dateString,null,startTimeString,durationString));
            assertEquals("Date, room type, start time and duration must have values assigned.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionDurationIsLessThan15Minutes() throws BadRequestApiRequestException {
            Exception exception = assertThrows(BadRequestApiRequestException.class, () -> sessionService.createAYogaSession(dateString,null,startTimeString,durationString));
            assertEquals("Date, room type, start time and duration must have values assigned.",exception.getMessage());
        }
        @Test
        void should_throwException409ConflictWithMessage_when_sessionAlreadyExists() throws ConflictApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(any(),any(),any())).thenReturn(session);
            Exception exception = assertThrows(ConflictApiRequestException.class,()-> sessionService.createAYogaSession(dateString,roomTypeString,startTimeString,durationString));
            assertEquals("Yoga session with same date,start time and room type already exists.",exception.getMessage());
        }
    }
    @Nested
    class AddMemberToYogaSession {
        @Test
        void should_addYogaSessionToPerson_when_addingPersonToSessionAndSessionNotContainsPerson(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            when(sessionServiceImpl.addMember(personOne,session)).thenReturn(true);

            sessionService.addMemberToYogaSession(session.getId(),personOne.getId());

            verify(personRepository,times(1)).save(personOne);
            verify(yogaSessionRepository,times(1)).save(session);
        }
        @Test
        void should_throwException404NotFound_when_addingPersonToSessionAndPersonNotExist(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.empty());
            Exception exception = assertThrows(NotFoundApiRequestException.class, () ->sessionService.addMemberToYogaSession(session.getId(),personOne.getId()));
            assertEquals("Person id:" + personOne.getId() + " couldn't be found.",exception.getMessage());
        }

        @Test
        void should_throwException404NotFound_when_addingPersonToSessionAndSessionNotExist(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.empty());
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            Exception exception = assertThrows(NotFoundApiRequestException.class, () ->sessionService.addMemberToYogaSession(session.getId(),personOne.getId()));
            assertEquals("Yoga session id:" + session.getId() +  " not found.",exception.getMessage());
        }


    }
    @Nested
    class RemoveMemberFromSession{

        @Test
        void should_returnTrueForRemove_when_personAndSessionExistAndPersonContainsSession(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(sessionServiceImpl.removeMember(personOne,session)).thenReturn(true);

            assertTrue(sessionService.removeMemberFromYogaSession(personOne.getId(),session.getId()));
            verify(yogaSessionRepository,times(1)).save(session);
            verify(personRepository,times(1)).save(personOne);
        }

        @Test
        void should_throwException404NotFound_when_removingPersonFromSessionAndSessionNotContainsPerson(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(sessionServiceImpl.removeMember(personOne,session)).thenThrow(new NotFoundApiRequestException("Person id:" + personOne.getId() + " not found in session id:" + session.getId()));

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.removeMemberFromYogaSession(personOne.getId(),session.getId()));
            assertEquals("Person id:" + personOne.getId() + " not found in session id:" + session.getId(), exception.getMessage());

            verify(yogaSessionRepository,times(0)).save(any());
            verify(personRepository,times(0)).save(any());
        }




    }

    @Test
    void should_passCorrectEndOfSessionValuesFromImpl(){
        when(sessionServiceImpl.getEndOfSession(session)).thenReturn(session.getEndOfSession());
        assertEquals(session.getEndOfSession(), sessionService.getEndOfSession(session));
    }

    @Test
    void should_callCalculateFreeSpace_when_sessionFoundInRepo(){
        when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(session.getDate(), session.getStartOfSession(), session.getRoomType()))
                .thenReturn(session);
        sessionService.getFreeSpace(session);
        verify(sessionServiceImpl,times(1)).getFreeSpace(session);
    }


    @Nested
    class SearchingForSessions{

        @Test
        void should_returnSessionList_when_searchingAllSessions(){
            List<YogaSession> sessionList = new ArrayList<>();
            sessionList.add(session);
            when(yogaSessionRepository.findAll()).thenReturn(sessionList);
            assertEquals(sessionList, sessionService.findAllSessions());
        }

        @Test
        void should_returnMinus1_whenSessionNotFoundInRepo(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(session.getDate(), session.getStartOfSession(), session.getRoomType()))
                    .thenReturn(null);
            assertEquals(-1,sessionService.getFreeSpace(session));
            verify(sessionServiceImpl,never()).getFreeSpace(session);
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_sessionNotFoundByIdInRepo(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.empty());
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.findYogaSessionById(session.getId()));

            assertEquals("Yoga session id:" + session.getId() + " not found.",exception.getMessage());
        }

        @Test
        void should_returnSession_when_sessionIsFoundByIdInRepo(){
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));

            assertEquals(session, sessionService.findYogaSessionById(12));
        }
    }
}
