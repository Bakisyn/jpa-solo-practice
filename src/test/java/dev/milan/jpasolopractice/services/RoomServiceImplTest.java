package dev.milan.jpasolopractice.services;


import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {
    private YogaSession session;
    private YogaSession sessionTwo;
    private Room roomOne;
    private Room roomTwo;
    private RoomServiceImpl roomServiceImplementation;
    private LocalTime min;
    private LocalTime max;



    @BeforeEach
    public void initialize(){
        roomServiceImplementation = new RoomServiceImpl();

        session = new YogaSession();
        session.setDate(LocalDate.now().plusDays(15));
        sessionTwo = new YogaSession();
        sessionTwo.setDate(LocalDate.now().plusDays(15));

        roomOne = new Room();
        roomOne.setRoomType(YogaRooms.AIR_ROOM);
//        session.setRoom(roomOne);
        roomOne.setDate(LocalDate.now().plus(15, ChronoUnit.DAYS));
        roomOne.setOpeningHours(LocalTime.of(8,0,0));
        roomOne.setClosingHours(LocalTime.of(22,0,0));
        roomTwo = new Room();
        roomTwo.setRoomType(YogaRooms.EARTH_ROOM);

        min = roomServiceImplementation.getMIN_OPENING_HOURS();
        max = roomServiceImplementation.getMAX_CLOSING_HOURS();

    }


    @Nested
    class AddSession{

        @Test
        void should_throwException400BadRequest_when_sessionAlreadyHasRoomAssigned(){
            session.setRoom(roomOne);
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session already has room assigned.",exception.getMessage());
        }


        @Test
        void should_throwException400BadRequest_when_addingSessionToRoomAndSessionAndRoomHaveDifferentDates(){
            roomOne.setDate(LocalDate.now());
            session.setDate(LocalDate.now().plusDays(1));
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session must have the same date as room.", exception.getMessage());
        }


        @Test
        void should_throwException400BadRequestWithMessage_when_sessionStartsBeforeRoomOpeningHours(){
            session.setStartOfSession(LocalTime.of(7,0,0));
            Exception exception = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session cannot start before room opening hours. Room opens at:" + roomOne.getOpeningHours(), exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionEndsAfterRoomClosingHours(){
            session.setStartOfSession(LocalTime.of(8,0,0));
            session.setEndOfSession(LocalTime.of(23,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session cannot end after room closing hours. Room closes at:" + roomOne.getClosingHours(),exception.getMessage());
        }
        @Test
        void should_returnTrue_when_addingSessionToRoomAndRoomSessionListIsEmpty(){
            session.setStartOfSession(LocalTime.of(8,0,0));
            session.setEndOfSession(LocalTime.of(10,0,0));
            assertTrue(roomServiceImplementation.addSessionToRoom(roomOne,session));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_sessionEndsAtTheSameTimeAlreadyPresentSessionEnds(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(9,0,0));
            session.setEndOfSession(LocalTime.of(12,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionStartsAtTheSameTimeAlreadyPresentSessionStarts(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(10,0,0));
            session.setEndOfSession(LocalTime.of(13,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionStartsBeforeTheEndAndAfterTheBeginningOfAlreadyPresentSession(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(11,0,0));
            session.setEndOfSession(LocalTime.of(13,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionStartsBeforeTheStartAndEndsBeforeTheEndOfAlreadyPresentSession(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(9,0,0));
            session.setEndOfSession(LocalTime.of(11,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_sessionStartsBeforeTheStartAndEndsAfterTheEndOfAlreadyPresentSession(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(9,0,0));
            session.setEndOfSession(LocalTime.of(13,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.addSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_returnTrue_when_sessionStartsAtAlreadyPresentSessionsEndAndSessionEndsAtAnotherPresentSessionsStart(){
            YogaSession sessionThree = new YogaSession();
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));

            sessionThree.setStartOfSession(LocalTime.of(14,0,0));
            sessionThree.setEndOfSession(LocalTime.of(15,0,0));
            sessionThree.setDate(LocalDate.now().plusDays(15));
            roomOne.addSession(sessionTwo);
            roomOne.addSession(sessionThree);

            session.setStartOfSession(LocalTime.of(12,0,0));
            session.setEndOfSession(LocalTime.of(14,0,0));
            assertTrue(roomServiceImplementation.addSessionToRoom(roomOne,session));
        }
    }

    @Nested
    class CreateARoom{
        @Test
        public void should_setOpeningHoursToMinHours_when_openingHoursBeforeMinHours(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),min.minusHours(2),LocalTime.of(20,0,0), YogaRooms.AIR_ROOM);
            assertEquals(min, roomOne.getOpeningHours());
        }
        @Test
        public void should_setClosingHoursToMaxHours_when_closingHoursAfterMaxHours(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),LocalTime.of(15,0,0),max.plusHours(2), YogaRooms.EARTH_ROOM);
            assertEquals(max,roomOne.getClosingHours());
        }
        @Test
        public void should_setOpeningHoursToMinHoursAndClosingHoursToMaxHours_when_openingHoursAfterClosingHours(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),LocalTime.of(15,0,0),LocalTime.of(12,0,0), YogaRooms.EARTH_ROOM);
            assertAll(
                    ()-> assertEquals(min,roomOne.getOpeningHours()),
                    ()-> assertEquals(max,roomOne.getClosingHours())
            );
        }
        @Test
        public void should_setOpeningHoursToMinHoursAndClosingHoursToMaxHours_when_openingHoursEqualsClosingHours(){
            roomOne = roomServiceImplementation.createARoom(LocalDate.now(),LocalTime.of(15,0,0),LocalTime.of(15,0,0), YogaRooms.EARTH_ROOM);
            assertAll(
                    ()-> assertEquals(min,roomOne.getOpeningHours()),
                    ()-> assertEquals(max,roomOne.getClosingHours())
            );
        }

        @Test
        public void should_setCorrectDate_when_settingDateInTheFuture(){
            Room room = roomServiceImplementation.createARoom(LocalDate.now().plusDays(1)
                    ,roomOne.getOpeningHours(),roomOne.getClosingHours(),roomOne.getRoomType());
            assertEquals(LocalDate.now().plusDays(1), room.getDate());
        }
        @Test
        public void should_setCurrentDate_when_settingCurrentDate(){
            Room room = roomServiceImplementation.createARoom(LocalDate.now()
                    ,roomOne.getOpeningHours(),roomOne.getClosingHours(),roomOne.getRoomType());
            assertEquals(LocalDate.now(), room.getDate());
        }

        @Test
        void should_throwExceptionBadRequest400WithMessage_when_creatingRoomWithDateInThePast(){
            Exception exception = assertThrows(BadRequestApiRequestException.class
                    , ()-> roomServiceImplementation.createARoom(LocalDate.now().minusDays(1),roomOne.getOpeningHours(),roomOne.getClosingHours()
                    ,roomOne.getRoomType()));
            assertEquals("Date cannot be before current date.",exception.getMessage());
        }
    }

    @Nested
    class CheckingFormattingOfPassedData{

        @Test
        void should_returnLocalDate_when_dateFormatCorrect(){
            LocalDate date = LocalDate.now().plusDays(2);
            String dateString = date.toString();
            assertEquals(date, roomServiceImplementation.checkDateFormat(dateString));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_dateFormatIncorrect(){
            Exception exception = assertThrows(ApiRequestException.class, ()-> roomServiceImplementation.checkDateFormat("12-2022-1"));
            assertEquals("Incorrect date. Correct format is: yyyy-mm-dd", exception.getMessage());
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_timeFormatIncorrect(){
            Exception exception = assertThrows(ApiRequestException.class, ()-> roomServiceImplementation.checkTimeFormat("25:01:10"));
            assertEquals("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59",exception.getMessage());
        }
        @Test
        void should_returnLocalTime_when_timeFormatCorrect(){
            LocalTime time = LocalTime.now().plusHours(1);
            String timeString = time.toString();
            assertEquals(time, roomServiceImplementation.checkTimeFormat(timeString));
        }
        @Test
        void should_returnYogaRoomType_when_yogRoomTypeFormatCorrect(){
            String type = YogaRooms.values()[0].name();
            YogaRooms roomType = YogaRooms.valueOf(type);
            assertEquals(roomType, roomServiceImplementation.checkRoomTypeFormat(type));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_yogaRoomTypeFormatIncorrect(){
            Exception exception = assertThrows(ApiRequestException.class, ()-> roomServiceImplementation.checkRoomTypeFormat("zzir"));
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<YogaRooms.values().length; i++){
                sb.append(" " + YogaRooms.values()[i].name());
                if (i < YogaRooms.values().length-1){
                    sb.append(",");
                }
            }
            assertEquals("Incorrect type. Correct options are:" + sb,exception.getMessage());
        }
    }




    @Test
    public void should_increaseNumberOfSessions_when_sessionAdded(){
        session.setStartOfSession(roomOne.getOpeningHours());
        session.setEndOfSession(session.getStartOfSession().plusHours(1));
        roomServiceImplementation.addSessionToRoom(roomOne,session);
        assertEquals(1,roomOne.getSessionList().size());
    }

    @Test
    public void should_removeYogaSessionFromRoom_when_roomContainsSession(){
        Room room = new Room();
        YogaSession session = new YogaSession();
        room.addSession(session);
        session.setRoom(room);

        assertTrue(roomServiceImplementation.removeSessionFromRoom(room,session));
    }
    @Test
    public void should_returnFalse_when_removingSessionFromRoomAndRoomNotContainsSession(){
        Room room = new Room();
        YogaSession session = new YogaSession();

        assertFalse(roomServiceImplementation.removeSessionFromRoom(room,session));
    }




}
