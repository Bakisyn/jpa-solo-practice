package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonService;
import dev.milan.jpasolopractice.service.YogaSessionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class YogaSessionServiceImplTest {
        private YogaSessionServiceImpl sessionServiceImpl;
        @Mock
        private PersonService personService;

        private YogaSession session;
        private Room room;
        private LocalDate date;
        private LocalTime startTime;
        private int duration;
        private YogaRooms roomType;
        private Person personOne;


        @BeforeEach
        void init(){
            personService = mock(PersonService.class);
            sessionServiceImpl = new YogaSessionServiceImpl(personService);
            room = new Room();
            roomType = YogaRooms.AIR_ROOM;
            room.setRoomType(roomType);
            date = LocalDate.now().plus(1, ChronoUnit.DAYS);
            startTime = LocalTime.of(10,0,0);
            duration = 60;

            session = new YogaSession();
            session.setRoom(room);
            session.setDate(date);
            session.setStartOfSession(startTime);
            session.setDuration(duration);

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
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
         assertEquals(session, temp);
     }
     @Test
     void should_setCurrentDate_when_passedDateInThePast()  {
         YogaSession temp = sessionServiceImpl.createAYogaSession(LocalDate.now().minusDays(2),room,startTime,duration);
         assertEquals(LocalDate.now(), temp.getDate());
     }

     @Test
     void should_throwException400BadRequestWithMessage_when_sessionRoomNull(){
         Exception exception = assertThrows(ApiRequestException.class,
                 ()-> sessionServiceImpl.createAYogaSession(date,null,startTime,duration));
         assertEquals("Session must have a room and session start time assigned.",exception.getMessage());
     }
     @Test
     void should_throwException400BadRequestWithMessage_when_startTimeLessThan30MinutesInAdvance(){
         room.setOpeningHours(LocalTime.now());
         Exception exception = Assertions.assertThrows(BadRequestApiRequestException.class,
                 ()-> sessionServiceImpl.createAYogaSession(LocalDate.now(),room,LocalTime.now().plusMinutes(15),duration));
         assertEquals("Must reserve a session at least 30 minutes in advance.",exception.getMessage());
     }

     @Test
     void should_throwException400BadRequestWithMessage_When_DateIsTodayAndCurrentTimeIsBeforeRoomOpenTime(){
         Exception exception = assertThrows(BadRequestApiRequestException.class,
                 ()-> sessionServiceImpl.createAYogaSession(LocalDate.now(),room,room.getOpeningHours().minus(10,ChronoUnit.MINUTES),duration));
         assertEquals("Yoga sessions start at: " + room.getOpeningHours() + ".",exception.getMessage());
     }

     @Test
     void should_setSessionDurationTo30_when_durationBelow30Passed() throws NotFoundApiRequestException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,25);
         assertEquals(30, temp.getDuration());
     }
     @Test
     void should_setCorrectEndOfSession() throws NotFoundApiRequestException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
         assertEquals(startTime.plusMinutes(duration), temp.getEndOfSession());
     }
     @Test
     void should_calculateCorrectRoomCapacity() throws NotFoundApiRequestException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
         assertEquals(YogaRooms.AIR_ROOM.getMaxCapacity(), temp.getFreeSpace());
     }
 }

    @Test
    void should_returnCorrectEndOfSession() throws NotFoundApiRequestException{
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
        assertEquals(temp.getEndOfSession(), sessionServiceImpl.getEndOfSession(temp));
    }

    @Test
    void should_returnFalse_when_sessionNotContainsPerson() throws NotFoundApiRequestException{
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
        assertFalse(sessionServiceImpl.removeMember(personOne, temp));
    }
    @Test
    void should_returnTrue_when_sessionContainsPerson() throws NotFoundApiRequestException {
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
        temp.addMember(personOne);
        temp.bookOneSpace();
        personOne.addSession(temp);
        when(personService.removeSessionFromPerson(personOne.getId(),temp.getId())).thenReturn(true);
        assertTrue(sessionServiceImpl.removeMember(personOne, temp));
    }

    @Test
    void should_returnCorrectlyContainsMember() throws ApiRequestException {
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
        temp.addMember(personOne);
        temp.bookOneSpace();
        personOne.addSession(temp);

        assertAll(
                ()-> assertFalse(sessionServiceImpl.containsMember(personOne,session)),
                ()-> assertTrue(sessionServiceImpl.containsMember(personOne,temp))
        );
    }
}
