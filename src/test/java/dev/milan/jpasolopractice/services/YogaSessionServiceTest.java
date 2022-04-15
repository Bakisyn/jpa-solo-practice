package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.RoomRepository;
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
    private RoomRepository roomRepository;
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
    private Room roomOne;
    private Room roomTwo;
    private List<YogaSession> sessionList;

    @BeforeEach
    void init(){
        date = today.plus(1, ChronoUnit.DAYS);

        roomOne = new Room();
        roomOne.setDate(today.plus(1,ChronoUnit.DAYS));
        roomOne.setOpeningHours(LocalTime.of(5,0,0));
        roomOne.setClosingHours(LocalTime.of(20,0,0));
        roomOne.setRoomType(RoomType.AIR_ROOM);
        roomOne.setTotalCapacity(RoomType.AIR_ROOM.getMaxCapacity());

        roomTwo = new Room();
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

        sessionList = new ArrayList<>();
        sessionList.add(session);
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


        @Test
        void should_returnSessionsForAllRoomsByDate_when_searchingAllSessionsFromAllRoomsByDateAndRoomsNotNull(){
            List<YogaSession> yogaSessions = new ArrayList<>();
            yogaSessions.add(session);
            yogaSessions.add(new YogaSession());

            List<Room> roomList = new ArrayList<>();
            roomList.add(roomOne);
            when(roomRepository.findAllRoomsByDate(any())).thenReturn(roomList);
            when(sessionServiceImpl.getAllRoomsSessionsInADay(roomList)).thenReturn(yogaSessions);
            when(formatCheckService.checkDateFormat(LocalDate.now().toString())).thenReturn(LocalDate.now());

            assertEquals(yogaSessions, sessionService.getAllRoomsSessionsInADay(LocalDate.now().toString()));
            verify(sessionServiceImpl, times(1)).getAllRoomsSessionsInADay(roomList);
            verify(roomRepository,times(1)).findAllRoomsByDate(LocalDate.now());
            verify(formatCheckService, times(1)).checkDateFormat(any());
        }

        @Test
        void should_throwException400BadRequest_when_searchingAllSessionsFromAllRoomsByDateAndDateFormatIncorrect(){
            when(formatCheckService.checkDateFormat(any())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));

            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> sessionService.getAllRoomsSessionsInADay("20-2022-1"));
            assertEquals("Incorrect date. Correct format is: yyyy-mm-dd",exception.getMessage());
        }
        @Test
        void should_throwException404NotFound_when_searchingAllSessionsFromAllRoomsByDateAndNoRoomsExist(){
            when(formatCheckService.checkDateFormat(any())).thenReturn(LocalDate.now());
            when(roomRepository.findAllRoomsByDate(LocalDate.now())).thenReturn(null);

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.getAllRoomsSessionsInADay("2022-02-01"));
            assertEquals("No rooms found on date:" + LocalDate.now(),exception.getMessage());
        }

        @Test
        void should_returnSessionsList_when_searchingSessionsInRoomRoomNotNull(){
            roomOne.addSession(session);
            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));

            assertEquals(roomOne.getSessionList(), sessionService.getSingleRoomSessionsInADay(roomOne.getId()));
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_searchingSessionsInRoomAndRoomIsNull(){
            when(roomRepository.findById(roomOne.getId())).thenThrow(new NotFoundApiRequestException("Room with id:" + roomOne.getId() + " doesn't exist."));
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.getSingleRoomSessionsInADay(roomOne.getId()));
            assertEquals("Room with id:" + roomOne.getId() + " doesn't exist.",exception.getMessage());
        }

    }

    @Nested
    class SearchingSessionsinRoomWithParams{

        @Test
        void should_throwApiRequestException_when_searchingSessionsByParamsAndPassedBadlyFormattedData(){
            when(formatCheckService.checkDateFormat(dateString)).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> sessionService.findSessionsByParams(Optional.of(dateString),Optional.empty()));
            assertEquals("Incorrect date. Correct format is: yyyy-mm-dd",exception.getMessage());
        }

        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeAllDatePresent(){
            when(formatCheckService.checkDateFormat(dateString)).thenReturn(date);
            when(yogaSessionRepository.findYogaSessionByDateAndRoomIsNotNull(date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.of("all")));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeAllDateNotPresent(){
            when(yogaSessionRepository.findYogaSessionByRoomIsNotNull()).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.of("all")));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNoneDatePresent(){
            when(formatCheckService.checkDateFormat(dateString)).thenReturn(date);
            when(yogaSessionRepository.findYogaSessionByDateAndRoomIsNull(date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.of("none")));
        }

        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNoneDateNotPresent(){
            when(yogaSessionRepository.findYogaSessionByRoomIsNull()).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.of("none")));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypePresentDatePresent(){
            when(formatCheckService.checkDateFormat(dateString)).thenReturn(date);
            when(formatCheckService.checkRoomTypeFormat(roomTypeString)).thenReturn(yogaRoomType);
            when(yogaSessionRepository.findYogaSessionByRoomTypeAndDateAndRoomIsNotNull(yogaRoomType,date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.of(roomTypeString)));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypePresentDateNotPresent(){
            when(formatCheckService.checkRoomTypeFormat(roomTypeString)).thenReturn(yogaRoomType);
            when(yogaSessionRepository.findYogaSessionByRoomTypeAndRoomIsNotNull(yogaRoomType)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.of(roomTypeString)));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNotPresentDatePresent(){
            when(formatCheckService.checkDateFormat(dateString)).thenReturn(date);
            when( yogaSessionRepository.findYogaSessionByDateAndRoomIsNotNull(date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.empty()));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNotPresentDateNotPresent(){
            when(yogaSessionRepository.findAll()).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.empty()));
        }
    }


}
