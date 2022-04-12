package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
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

    private LocalDate date;
    private Room roomOne;
    private Room roomTwo;
    private LocalTime startTime;
    private int duration;
    private YogaSession session;
    private Person personOne;

    @BeforeEach
    void init(){
        date = LocalDate.now().plus(1, ChronoUnit.DAYS);

        roomOne = new Room();
        roomOne.setDate(LocalDate.now().plus(1,ChronoUnit.DAYS));
        roomOne.setOpeningHours(LocalTime.of(5,0,0));
        roomOne.setClosingHours(LocalTime.of(20,0,0));
        roomOne.setRoomType(YogaRooms.AIR_ROOM);
        roomOne.setTotalCapacity(YogaRooms.AIR_ROOM.getMaxCapacity());

        roomTwo = new Room();
        roomTwo.setRoomType(YogaRooms.EARTH_ROOM);
        roomTwo.setDate(LocalDate.now().plus(1,ChronoUnit.DAYS));
        roomTwo.setOpeningHours(LocalTime.of(9,0,0));
        roomTwo.setClosingHours(LocalTime.of(23,0,0));
        roomTwo.setTotalCapacity(YogaRooms.EARTH_ROOM.getMaxCapacity());

        startTime = LocalTime.of(10,0,0);
        duration = 60;

        session = new YogaSession();
        session.setRoom(roomOne);
        session.setStartOfSession(LocalTime.of(8,0,0));
        session.setDuration(45);
        session.setDate(LocalDate.now());

        personOne = new Person();
        personOne.setEmail("example@hotmail.com");
        personOne.setAge(33);
        personOne.setName("Badji");
        personOne.setName("Kukumber");
    }

    @Nested
    class CreateYogaSession{
        @Test
        void should_createYogaSession_when_sessionInfoCorrect() throws NotFoundApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
            when(sessionServiceImpl.createAYogaSession(any(),any(),any(),anyInt())).thenReturn(session);

            assertEquals(session, sessionService.createAYogaSession(date, roomOne,startTime,duration));
        }
        @Test
        void should_throwException400BadRequest_when_creatingSessionAndRoomIsNull() throws BadRequestApiRequestException {
            assertThrows(BadRequestApiRequestException.class,() ->  sessionService.createAYogaSession(date, null,startTime,duration));
        }
        @Test
        void should_throwException400BadRequest_when_creatingSessionAndDateIsNull() throws BadRequestApiRequestException {
            assertThrows(BadRequestApiRequestException.class,() ->  sessionService.createAYogaSession(null, roomOne,startTime,duration));
        }
        @Test
        void should_saveSessionInRepo_when_sessionInfoCorrect() throws NotFoundApiRequestException {
            ArgumentCaptor<YogaSession> sessionCaptor = ArgumentCaptor.forClass(YogaSession.class);
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
            when(sessionServiceImpl.createAYogaSession(any(),any(),any(),anyInt())).thenReturn(session);

            sessionService.createAYogaSession(date, roomOne,startTime,duration);

            verify(yogaSessionRepository,times(1)).save(sessionCaptor.capture());
            assertEquals(session, sessionCaptor.getValue());
        }

        @Test
        void should_notSaveSession_when_sessionInfoIncorrect() {
            try{
                when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
                when(sessionServiceImpl.createAYogaSession(any(),any(),any(),anyInt())).thenThrow(new NotFoundApiRequestException(""));
                sessionService.createAYogaSession(date, roomOne,startTime,duration);
                fail();
            }catch (ApiRequestException e){

            }finally {
                verify(yogaSessionRepository,never()).save(any());
            }
        }

        @Test
        void should_throwException400BadRequest_When_SessionInfoIncorrect() throws BadRequestApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(null);
            when(sessionServiceImpl.createAYogaSession(any(),any(),any(),anyInt())).thenThrow(new BadRequestApiRequestException(""));
            Executable executable = () -> sessionService.createAYogaSession(date,roomOne,startTime,duration);
            assertThrows(BadRequestApiRequestException.class, executable);
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionDateIsNull() throws BadRequestApiRequestException {
            Exception exception = assertThrows(BadRequestApiRequestException.class, () -> sessionService.createAYogaSession(null,roomOne,startTime,duration));
            assertEquals("Date and room must not be null.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionRoomIsNull() throws BadRequestApiRequestException {
            Exception exception = assertThrows(BadRequestApiRequestException.class, () -> sessionService.createAYogaSession(date,null,startTime,duration));
            assertEquals("Date and room must not be null.",exception.getMessage());
        }

        @Test
        void should_throwException409ConflictWithMessage_when_sessionAlreadyExists() throws ConflictApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(session);
            Exception exception = assertThrows(ConflictApiRequestException.class, ()-> sessionService.createAYogaSession(date,roomOne,startTime,duration));
            assertEquals("Yoga session with same date,start time and room already exists.",exception.getMessage());
        }
    }
    @Nested
    class AddMemberToYogaSession {
        @Test
        void should_addYogaSessionToPerson_when_personAndSessionExist(){
            Person spyPerson = spy(Person.class);
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(session);
            when(personRepository.findPersonByEmail(spyPerson.getEmail())).thenReturn(spyPerson);
            when(sessionServiceImpl.addMember(spyPerson,session)).thenReturn(true);

            sessionService.addMemberToYogaSession(spyPerson,session);

            verify(spyPerson,times(1)).addSession(session);
        }
        @Test
        void should_returnTrue_when_personAndSessionExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(session);
            when(personRepository.findPersonByEmail(personOne.getEmail())).thenReturn(personOne);
            when(sessionServiceImpl.addMember(personOne,session)).thenReturn(true);

            assertTrue(sessionService.addMemberToYogaSession(personOne,session));
        }

        @Test
        void should_savePersonAndSessionToRepo_when_personAndSessionExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(session);
            when(personRepository.findPersonByEmail(personOne.getEmail())).thenReturn(personOne);
            when(sessionServiceImpl.addMember(personOne,session)).thenReturn(true);

            sessionService.addMemberToYogaSession(personOne,session);

            verify(yogaSessionRepository,times(1)).save(session);
            verify(personRepository,times(1)).save(personOne);
        }

        @Test
        void should_returnFalse_when_yogaSessionNotExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
            assertFalse(sessionService.addMemberToYogaSession(personOne,session));
        }
        @Test
        void should_returnFalse_when_yogaSessionExistsAndPersonNotExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(session);
            when(personRepository.findPersonByEmail(any())).thenReturn(null);
            assertFalse(sessionService.addMemberToYogaSession(personOne,session));
        }
    }
    @Nested
    class RemoveMemberFromSession{

        @Test
        void should_returnTrueForRemove_when_personAndSessionExistAndPersonContainsSession(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(sessionServiceImpl.removeMember(personOne,session)).thenReturn(true);

            assertTrue(sessionService.removeMemberFromYogaSession(personOne,session));
        }

        @Test
        void should_saveToRepoAfterRemove_when_personAndSessionExistAndPersonContainsSession(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(sessionServiceImpl.removeMember(personOne,session)).thenReturn(true);

            sessionService.removeMemberFromYogaSession(personOne,session);

            verify(yogaSessionRepository,times(1)).save(session);
            verify(personRepository,times(1)).save(personOne);
        }

        @Test
        void should_returnFalseForRemove_when_personNotExist(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.empty());
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

            assertFalse(sessionService.removeMemberFromYogaSession(personOne,session));
        }

        @Test
        void should_returnTrueForRemove_when_sessionNotExist(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.empty());

            assertFalse(sessionService.removeMemberFromYogaSession(personOne,session));
        }

        @Test
        void should_returnFalseForRemove_when_personNotContainsSession(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(sessionServiceImpl.removeMember(personOne,session)).thenReturn(false);

            assertFalse(sessionService.removeMemberFromYogaSession(personOne,session));
        }
        @Test
        void should_notSaveToRepository_when_personNotContainsSession(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(sessionServiceImpl.removeMember(personOne,session)).thenReturn(false);

            sessionService.removeMemberFromYogaSession(personOne,session);

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
        when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(session.getDate(), session.getStartOfSession(), session.getRoom()))
                .thenReturn(session);
        sessionService.getFreeSpace(session);
        verify(sessionServiceImpl,times(1)).getFreeSpace(session);
    }

    @Test
    void should_returnMinus1_whenSessionNotFoundInRepo(){
        when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(session.getDate(), session.getStartOfSession(), session.getRoom()))
                .thenReturn(null);
        assertEquals(-1,sessionService.getFreeSpace(session));
        verify(sessionServiceImpl,never()).getFreeSpace(session);
    }

    @Test
    void should_throwException404NotFoundWithMessage_when_sessionNotFoundByIdInRepo(){
        when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.empty());
        Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.findYogaSessionById(20));

        assertEquals("Yoga session with that id couldn't be found.",exception.getMessage());
    }

    @Test
    void should_returnSession_when_sessionIsFoundByIdInRepo(){
        when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));

        assertEquals(session, sessionService.findYogaSessionById(12));
    }
}
