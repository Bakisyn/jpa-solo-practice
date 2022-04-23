package dev.milan.jpasolopractice.yogasession.util;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ForbiddenApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.person.PersonService;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.util.YogaSessionUtilImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YogaSessionUtilImplTest {
        private YogaSessionUtilImpl yogaSessionUtilImpl;
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
            yogaSessionUtilImpl = new YogaSessionUtilImpl(personService);
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
     void should_createSessionWithCorrectValues_when_creatingYogaSession_and_correctValuesPassed() throws NotFoundApiRequestException {
         YogaSession temp = yogaSessionUtilImpl.createAYogaSession(date,roomType,startTime,duration);
         assertEquals(session, temp);
     }
     @Test
     void should_setCurrentDate_when_creatingYogaSession_and_passedDateInThePast()  {
         YogaSession temp = yogaSessionUtilImpl.createAYogaSession(today.minusDays(2),roomType,startTime,duration);
         assertEquals(today, temp.getDate());
     }

     @Test
     void should_throwException400BadRequestWithMessage_when_creatingYogaSession_and_startTimeLessThan30MinutesInAdvance(){
         room.setOpeningHours(LocalTime.now());
         Exception exception;
         int afterMinutes = yogaSessionUtilImpl.getReserveInAdvanceAmount() - 10;
         if (LocalTime.now().isAfter(yogaSessionUtilImpl.getLatestReservation())){
             exception = Assertions.assertThrows(BadRequestApiRequestException.class,
                     ()-> yogaSessionUtilImpl.createAYogaSession(today,roomType,LocalTime.now().plusMinutes(afterMinutes),duration));
             assertEquals("Can't reserve a session on date:" + today + " after time:" + yogaSessionUtilImpl.getLatestReservation(),exception.getMessage());
         }else{
             exception = Assertions.assertThrows(BadRequestApiRequestException.class,
                     ()-> yogaSessionUtilImpl.createAYogaSession(today,roomType,LocalTime.now().plusMinutes(afterMinutes),duration));
             assertEquals("Must reserve a session at least "+ yogaSessionUtilImpl.getReserveInAdvanceAmount() + " minutes in advance.",exception.getMessage());
         }

     }

     @Test
     void should_setException400BadRequest_when_creatingYogaSession_and_durationBelow30Passed() throws NotFoundApiRequestException {
         Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> yogaSessionUtilImpl.createAYogaSession(date,roomType,startTime, yogaSessionUtilImpl.getMinDuration()-1));
         assertEquals("Session duration cannot be less than " + yogaSessionUtilImpl.getMinDuration(), exception.getMessage());
     }
     @Test
     void should_setCorrectEndOfSession_when_creatingYogaSession() throws NotFoundApiRequestException {
         YogaSession temp = yogaSessionUtilImpl.createAYogaSession(date,roomType,startTime,duration);
         assertEquals(startTime.plusMinutes(duration), temp.getEndOfSession());
     }
     @Test
     void should_throwException400BadRequest_when_creatingYogaSession_and_endOfSessionAtMidnightOrLater(){
         Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> yogaSessionUtilImpl.createAYogaSession(date,roomType,LocalTime.of(22,15,0),106));
         assertEquals("Ending time must be at the before or equal to " + yogaSessionUtilImpl.getLatestSessionEnding(),exception.getMessage());
     }

     @Test
     void should_calculateCorrectRoomCapacity_when_creatingYogaSession() throws NotFoundApiRequestException {
         YogaSession temp = yogaSessionUtilImpl.createAYogaSession(date,roomType,startTime,duration);
         assertEquals(RoomType.AIR_ROOM.getMaxCapacity(), temp.getFreeSpace());
     }
 }


    @Test
    void should_returnFalse_when_removingPersonFromSession_and_sessionDoesntContainPerson() throws NotFoundApiRequestException{
        YogaSession temp = yogaSessionUtilImpl.createAYogaSession(date,roomType,startTime,duration);
        Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> yogaSessionUtilImpl.removeMember(personOne, temp));

        assertEquals("Person id:" + personOne.getId() + " not found in session id:" + session.getId(), exception.getMessage());

    }
    @Test
    void should_returnTrue_when_removingPersonFromSession_and_sessionContainsPerson() throws NotFoundApiRequestException {
        YogaSession temp = yogaSessionUtilImpl.createAYogaSession(date,roomType,startTime,duration);
        temp.addMember(personOne);
        temp.bookOneSpace();
        personOne.addSession(temp);
        when(personService.removeSessionFromPerson(personOne,temp)).thenReturn(true);
        assertTrue(yogaSessionUtilImpl.removeMember(personOne, temp));
    }

    @Test
    void should_returnCorrectlyContainsMember_when_addingPersonToSession_and_sessionDoesntContainPerson() throws ApiRequestException {
        YogaSession temp = yogaSessionUtilImpl.createAYogaSession(date,roomType,startTime,duration);
        temp.addMember(personOne);
        temp.bookOneSpace();
        personOne.addSession(temp);

        assertAll(
                ()-> assertFalse(yogaSessionUtilImpl.containsMember(personOne,session)),
                ()-> assertTrue(yogaSessionUtilImpl.containsMember(personOne,temp))
        );
    }
    @Nested
    class AddingAndRemovingPersonToSession{
        @Test
        void should_addPersonToSession_when_addingSessionToPerson_and_sessionDoesntContainPerson(){
            when(personService.addSessionToPerson(session,personOne)).thenReturn(true);
            assertTrue(yogaSessionUtilImpl.addMember(personOne,session));
        }
        @Test
        void should_throwException409Conflict_when_addingPersonToSession_and_sessionContainsPerson(){
            session.addMember(personOne);
            Exception exception = assertThrows(ConflictApiRequestException.class,()-> yogaSessionUtilImpl.addMember(personOne,session));
            assertEquals("User id:" + personOne.getId() + " already present in session id:" + session.getId(),exception.getMessage());
        }
        @Test
        void should_throwException403Forbidden_when_addingPersonToSession_and_sessionHasNoRoomLeft(){
            for (int i = 0; i< roomType.getMaxCapacity(); i++){
                session.bookOneSpace();
            }
            when(personService.addSessionToPerson(session,personOne)).thenReturn(true);

            Exception exception = assertThrows(ForbiddenApiRequestException.class,()-> yogaSessionUtilImpl.addMember(personOne,session));
            assertEquals("Session id:" + session.getId() + " member limit reached.",exception.getMessage());
        }

        @Test
        void should_throwException400NotFound_when_removingPersonFromSession_and_personNotFound(){
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> yogaSessionUtilImpl.removeMember(personOne,session));
            assertEquals(("Person id:" + personOne.getId() + " not found in session id:" + session.getId()),exception.getMessage());
        }
        @Test
        void should_removePersonFromSession_when_removingPersonFromSession_and_personFoundInSession(){
            when(personService.removeSessionFromPerson(personOne,session)).thenReturn(true);
            session.addMember(personOne);
            session.bookOneSpace();
            yogaSessionUtilImpl.removeMember(personOne,session);
            assertTrue(session.getMembersAttending().isEmpty());
        }
    }
}
