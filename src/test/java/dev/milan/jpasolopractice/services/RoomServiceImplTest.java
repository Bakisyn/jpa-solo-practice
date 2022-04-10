package dev.milan.jpasolopractice.services;


import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.RoomServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {
//    private AutoCloseable closeable;
    YogaSession session;
    Room roomOne;
    Room roomTwo;

    RoomServiceImpl roomServiceImplementation;




    @BeforeEach
    public void initialize(){
        roomServiceImplementation = new RoomServiceImpl();

        session = new YogaSession();
        session.setDate(LocalDate.now().plus(15, ChronoUnit.DAYS));
        roomOne = new Room();
        roomOne.setRoomType(YogaRooms.AIR_ROOM);
        session.setRoom(roomOne);
        roomOne.setDate(LocalDate.now().plus(15, ChronoUnit.DAYS));
        roomOne.setOpeningHours(LocalTime.of(8,0,0));
        roomTwo = new Room();
        roomTwo.setRoomType(YogaRooms.EARTH_ROOM);


    }


    @Nested
    class AddSession{
        @Test
        public void should_ReturnFalse_IfNoRoomInSession(){
            session.setRoom(null);
            assertFalse(roomServiceImplementation.addSessionToRoom(roomTwo,session));
        }
        @Test
        public void should_ReturnTrue_IfRoomTypeIsSame(){
            session.setRoom(roomOne);
            assertTrue(roomServiceImplementation.addSessionToRoom(roomOne,session));
        }

        @Test
        void should_ReturnFalse_IfSessionDurationIsMoreThanWhatIsLeftBeforeClosingHours(){
            session.setRoom(roomOne);
            session.setDuration(1200);
            assertFalse(roomServiceImplementation.addSessionToRoom(roomOne,session));
        }

        @Test
        public void should_ReturnFalse_IfRoomTypeIsDifferent(){
            session.setRoom(roomTwo);
            assertFalse(roomServiceImplementation.addSessionToRoom(roomTwo,session));
        }
    }

    @Nested
    class CreateARoom{
        @Test
        public void should_SetOpeningHoursTo8_When_SettingOpeningHoursToBefore8(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),LocalTime.of(5,0,0),LocalTime.of(20,0,0), YogaRooms.AIR_ROOM);
            assertEquals(LocalTime.of(8,0,0), roomOne.getOpeningHours());
        }
        @Test
        public void should_SetClosingHoursTo22_When_SettingClosingHoursToAfter22(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),LocalTime.of(15,0,0),LocalTime.of(23,0,0), YogaRooms.EARTH_ROOM);
            assertEquals(LocalTime.of(22,0,0),roomOne.getClosingHours());
        }
        @Test
        public void should_SetOpeningHoursTo8AndClosingHoursTo22_When_OpeningSettingOpeningHoursAfterClosingHours(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),LocalTime.of(15,0,0),LocalTime.of(12,0,0), YogaRooms.EARTH_ROOM);
            assertAll(
                    ()-> assertEquals(LocalTime.of(8,0,0),roomOne.getOpeningHours()),
                    ()-> assertEquals(LocalTime.of(22,0,0),roomOne.getClosingHours())
            );
        }
        @Test
        public void should_SetOpeningHoursTo8AndClosingHoursTo22_When_OpeningSettingOpeningHoursEqualsClosingHours(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),LocalTime.of(15,0,0),LocalTime.of(15,0,0), YogaRooms.EARTH_ROOM);
            assertAll(
                    ()-> assertEquals(LocalTime.of(8,0,0),roomOne.getOpeningHours()),
                    ()-> assertEquals(LocalTime.of(22,0,0),roomOne.getClosingHours())
            );
        }

        @Test
        public void should_SetCurrentDate_When_SettingADateInThePast(){ //should test in room impl
            roomOne = roomServiceImplementation.createARoom(LocalDate.of(2021,9,1),LocalTime.of(10,0,0),LocalTime.of(12,0,0), YogaRooms.EARTH_ROOM);
            assertEquals(LocalDate.now(), roomOne.getDate());
        }
    }


    @Test
    public void should_IncreaseNumberOfSessions_When_ASessionIsAdded(){  //should test in room impl
        roomServiceImplementation.addSessionToRoom(roomOne,session);
        assertEquals(1,roomOne.getSessionList().size());
    }

    @Test
    public void should_SuccessfullyRemoveYogaSessionFromRoom_when_RoomContainsSession(){
        Room room = new Room();
        YogaSession session = new YogaSession();
        room.addSession(session);
        session.setRoom(room);

        assertTrue(roomServiceImplementation.removeSessionFromRoom(room,session));
    }
    @Test
    public void should_returnFalseWhenRemovingSessionFromARoom_when_RoomDoesntContainSession(){
        Room room = new Room();
        YogaSession session = new YogaSession();

        assertFalse(roomServiceImplementation.removeSessionFromRoom(room,session));
    }

}
