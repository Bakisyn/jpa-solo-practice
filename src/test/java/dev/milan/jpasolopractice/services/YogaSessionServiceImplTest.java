package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ForbiddenApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.RoomType;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonService;
import dev.milan.jpasolopractice.service.YogaSessionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YogaSessionServiceImplTest {
        private YogaSessionServiceImpl sessionServiceImpl;
        @Mock
        private PersonService personService;

        private YogaSession session;
        private Room room;
        private LocalDate date;
        private LocalTime startTime;
        private int duration;
        private RoomType roomType;
        private Person personOne;
        private final LocalDate today = LocalDate.now();



        @BeforeEach
        void init(){
            personService = mock(PersonService.class);
            sessionServiceImpl = new YogaSessionServiceImpl(personService);
            room = new Room();
            roomType = RoomType.AIR_ROOM;
            room.setRoomType(roomType);
            date = today.plus(1, ChronoUnit.DAYS);
            startTime = LocalTime.of(10,0,0);
            duration = 60;

            session = new YogaSession();
            session.setDate(date);
            session.setStartOfSession(startTime);
            session.setDuration(duration);
            session.setRoomType(roomType);

            personOne = new Person();
            personOne.setEmail("example@hotmail.com");
            personOne.setAge(33);
            personOne.setName("Badji");
            personOne.setName("Kukumber");
        }
    @Nested
 class CreateAYogaSession{
     @Test
     void should_createASessionWithCorrectValues_when_correctValuesPassed() throws NotFoundApiRequestException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
         assertEquals(session, temp);
     }
     @Test
     void should_setCurrentDate_when_passedDateInThePast()  {
         YogaSession temp = sessionServiceImpl.createAYogaSession(today.minusDays(2),roomType,startTime,duration);
         assertEquals(today, temp.getDate());
     }

     @Test
     void should_throwException400BadRequestWithMessage_when_startTimeLessThan30MinutesInAdvance(){
         room.setOpeningHours(LocalTime.now());
         Exception exception = Assertions.assertThrows(BadRequestApiRequestException.class,
                 ()-> sessionServiceImpl.createAYogaSession(today,roomType,LocalTime.now().plusMinutes(15),duration));
         assertEquals("Must reserve a session at least 30 minutes in advance.",exception.getMessage());
     }

     @Test
     void should_setSessionDurationTo30_when_durationBelow30Passed() throws NotFoundApiRequestException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,25);
         assertEquals(30, temp.getDuration());
     }
     @Test
     void should_setCorrectEndOfSession() throws NotFoundApiRequestException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
         assertEquals(startTime.plusMinutes(duration), temp.getEndOfSession());
     }
     @Test
     void should_calculateCorrectRoomCapacity() throws NotFoundApiRequestException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
         assertEquals(RoomType.AIR_ROOM.getMaxCapacity(), temp.getFreeSpace());
     }
 }

    @Test
    void should_returnCorrectEndOfSession() throws NotFoundApiRequestException{
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
        assertEquals(temp.getEndOfSession(), sessionServiceImpl.getEndOfSession(temp));
    }

    @Test
    void should_returnFalse_when_sessionNotContainsPerson() throws NotFoundApiRequestException{
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
        Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionServiceImpl.removeMember(personOne, temp));

        assertEquals("Person id:" + personOne.getId() + " not found in session id:" + session.getId(), exception.getMessage());

    }
    @Test
    void should_returnTrue_when_sessionContainsPerson() throws NotFoundApiRequestException {
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
        temp.addMember(personOne);
        temp.bookOneSpace();
        personOne.addSession(temp);
        when(personService.removeSessionFromPerson(personOne,temp)).thenReturn(true);
        assertTrue(sessionServiceImpl.removeMember(personOne, temp));
    }

    @Test
    void should_returnCorrectlyContainsMember() throws ApiRequestException {
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,roomType,startTime,duration);
        temp.addMember(personOne);
        temp.bookOneSpace();
        personOne.addSession(temp);

        assertAll(
                ()-> assertFalse(sessionServiceImpl.containsMember(personOne,session)),
                ()-> assertTrue(sessionServiceImpl.containsMember(personOne,temp))
        );
    }
    @Nested
    class AddingAndRemovingPersonToSession{
        @Test
        void should_addPersonToSession_when_sessionNotContainsPerson(){
            when(personService.addSessionToPerson(session,personOne)).thenReturn(true);
            assertTrue(sessionServiceImpl.addMember(personOne,session));
        }
        @Test
        void should_throwException409Conflict_when_sessionContainsPerson(){
            session.addMember(personOne);
            Exception exception = assertThrows(ConflictApiRequestException.class,()-> sessionServiceImpl.addMember(personOne,session));
            assertEquals("User id:" + personOne.getId() + " already present in session id:" + session.getId(),exception.getMessage());
        }
        @Test
        void should_throwException403Forbidden_when_sessionHasNoRoomLeft(){
            for (int i = 0; i< roomType.getMaxCapacity(); i++){
                session.bookOneSpace();
            }
            when(personService.addSessionToPerson(session,personOne)).thenReturn(true);

            Exception exception = assertThrows(ForbiddenApiRequestException.class,()->sessionServiceImpl.addMember(personOne,session));
            assertEquals("Session id:" + session.getId() + " member limit reached.",exception.getMessage());
        }

        @Test
        void should_throwException400NotFound_when_removingPersonFromSessionAndPersonNotFound(){
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionServiceImpl.removeMember(personOne,session));
            assertEquals(("Person id:" + personOne.getId() + " not found in session id:" + session.getId()),exception.getMessage());
        }
        @Test
        void should_removePersonFromSession_when_personFoundInSession(){
            when(personService.removeSessionFromPerson(personOne,session)).thenReturn(true);
            session.addMember(personOne);
            session.bookOneSpace();
            sessionServiceImpl.removeMember(personOne,session);
            assertTrue(session.getMembersAttending().isEmpty());
        }
    }
}
