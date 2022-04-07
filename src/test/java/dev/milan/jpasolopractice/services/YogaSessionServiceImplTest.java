package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.SessionNotAvailableException;
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

import java.lang.reflect.Executable;
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
     void should_CreateASessionWithCorrectValues_When_CorrectValuesPassed() throws SessionNotAvailableException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
         assertEquals(session, temp);
     }
     @Test
     void should_SetCurrentDate_When_PassedADateInThePast() throws SessionNotAvailableException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(LocalDate.now().minusDays(2),room,startTime,duration);
         assertEquals(LocalDate.now(), temp.getDate());
     }

     @Test
     void should_ThrowSessionNotAvailableException_When_SessionRoomIsNull(){
         SessionNotAvailableException thrown = Assertions.assertThrows(SessionNotAvailableException.class,
                 ()-> sessionServiceImpl.createAYogaSession(date,null,startTime,duration));
         assertEquals("Session must have a room and session start time assigned.",thrown.getMessage());
     }
     @Test
     void should_ThrowSessionNotAvailableException_When_StartTimeIsLessThan30MinutesInAdvance(){
         room.setOpeningHours(LocalTime.now());
         SessionNotAvailableException thrown = Assertions.assertThrows(SessionNotAvailableException.class,
                 ()-> sessionServiceImpl.createAYogaSession(LocalDate.now(),room,LocalTime.now().plusMinutes(15),duration));
         assertEquals("Must reserve a session at least 30 minutes in advance.",thrown.getMessage());
     }

     @Test
     void should_ThrowSessionNotAvailableException_When_DateIsTodayAndCurrentTimeIsBeforeRoomOpenTime(){
         SessionNotAvailableException thrown = Assertions.assertThrows(SessionNotAvailableException.class,
                 ()-> sessionServiceImpl.createAYogaSession(LocalDate.now(),room,room.getOpeningHours().minus(10,ChronoUnit.MINUTES),duration));
         assertEquals("Yoga sessions start at: " + room.getOpeningHours(),thrown.getMessage());
     }

     @Test
     void should_SetSessionDurationTo30_When_DurationBelow30IsPassed() throws SessionNotAvailableException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,25);
         assertEquals(30, temp.getDuration());
     }
     @Test
     void should_SetCorrectEndOfSession() throws SessionNotAvailableException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
         assertEquals(startTime.plusMinutes(duration), temp.getEndOfSession());
     }
     @Test
     void should_CalculateCorrectRoomCapacity() throws SessionNotAvailableException {
         YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
         assertEquals(YogaRooms.AIR_ROOM.getMaxCapacity(), temp.getFreeSpace());
     }
 }

    @Test
    void should_ReturnCorrectEndOfSession() throws SessionNotAvailableException{
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
        assertEquals(temp.getEndOfSession(), sessionServiceImpl.getEndOfSession(temp));
    }

//    public boolean removeMember(Person person, YogaSession session, PersonService personService) {
//        if(containsMember(person,session)){
//            if (personService.removeSession(person,session)){
//                session.removeMember(person);
//                removeOneBooked(session);
//                return true;
//            }
//        }
//        return false;
//    }
    @Test
    void should_ReturnFalseFromRemoveMember_When_SessionDoesntContainPerson() throws SessionNotAvailableException{
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
        assertFalse(sessionServiceImpl.removeMember(personOne, temp));
    }
    @Test
    void should_ReturnTrueFromRemoveMember_When_SessionContainsPerson() throws  SessionNotAvailableException{
        YogaSession temp = sessionServiceImpl.createAYogaSession(date,room,startTime,duration);
        temp.addMember(personOne);
        temp.bookOneSpace();
        personOne.addSession(temp);
        when(personService.removeSession(personOne,temp)).thenReturn(true);
        assertTrue(sessionServiceImpl.removeMember(personOne, temp));
    }

    @Test
    void should_ReturnCorrectlyContainsMember() throws SessionNotAvailableException{
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
