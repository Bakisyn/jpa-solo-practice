package dev.milan.jpasolopractice.room.util;


import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RoomUtilImplTest {
    private YogaSession session;
    private YogaSession sessionTwo;
    private Room roomOne;
    private RoomUtilImpl roomServiceImplementation;
    private LocalTime min;
    private LocalTime max;
    private final LocalDate today = LocalDate.now();


    @BeforeEach
    public void initialize(){
        roomServiceImplementation = new RoomUtilImpl();

        session = new YogaSession();
        session.setDate(today.plusDays(15));
        sessionTwo = new YogaSession();
        sessionTwo.setDate(today.plusDays(15));
        sessionTwo.setRoomType(RoomType.values()[1]);
        session.setRoomType(RoomType.values()[0]);

        roomOne = new Room();
        roomOne.setRoomType(RoomType.AIR_ROOM);
        roomOne.setDate(today.plus(15, ChronoUnit.DAYS));
        roomOne.setOpeningHours(LocalTime.of(8,0,0));
        roomOne.setClosingHours(LocalTime.of(22,0,0));
        Room roomTwo = new Room();
        roomTwo.setRoomType(RoomType.EARTH_ROOM);

        min = roomServiceImplementation.getMinOpeningHours();
        max = roomServiceImplementation.getMaxClosingHours();

    }


    @Nested
    class AddSessionToRoom{

        @Test
        void should_throwException400BadRequest_when_addingSessionToRoom_and_sessionAlreadyHasRoomAssigned(){
            session.setRoom(roomOne);
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session already has room assigned.",exception.getMessage());
        }

        @Test
        void should_throwException400BadRequest_when_addingSessionToRoom_and_sessionAndRoomHaveRoomTypeMismatch(){
            session.setRoomType(RoomType.values()[1]);
            roomOne.setRoomType(RoomType.values()[0]);
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session and room must have a matching room type.", exception.getMessage());
        }


        @Test
        void should_throwException400BadRequest_when_addingSessionToRoom_and_addingSessionToRoomAndSessionAndRoomHaveDifferentDates(){
            roomOne.setDate(today);
            session.setDate(today.plusDays(1));
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session must have the same date as room.", exception.getMessage());
        }


        @Test
        void should_throwException400BadRequestWithMessage_when_addingSessionToRoom_and_sessionStartsBeforeRoomOpeningHours(){
            session.setStartOfSession(LocalTime.of(7,0,0));
            Exception exception = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session cannot start before room opening hours. Room opens at:" + roomOne.getOpeningHours(), exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_addingSessionToRoom_and_sessionEndsAfterRoomClosingHours(){
            session.setStartOfSession(LocalTime.of(8,0,0));
            session.setEndOfSession(LocalTime.of(23,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session cannot end after room closing hours. Room closes at:" + roomOne.getClosingHours(),exception.getMessage());
        }
        @Test
        void should_returnTrue_when_addingSessionToRoom_and_roomSessionListIsEmpty(){
            session.setStartOfSession(LocalTime.of(8,0,0));
            session.setEndOfSession(LocalTime.of(10,0,0));
            assertTrue(roomServiceImplementation.canAddSessionToRoom(roomOne,session));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_addingSessionToRoom_and_sessionEndsAtTheSameTimeAlreadyPresentSessionEnds(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(9,0,0));
            session.setEndOfSession(LocalTime.of(12,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_addingSessionToRoom_and_sessionStartsAtTheSameTimeAlreadyPresentSessionStarts(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(10,0,0));
            session.setEndOfSession(LocalTime.of(13,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_addingSessionToRoom_and_sessionStartsBeforeTheEndAndAfterTheBeginningOfAlreadyPresentSession(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(11,0,0));
            session.setEndOfSession(LocalTime.of(13,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_addingSessionToRoom_and_sessionStartsBeforeTheStartAndEndsBeforeTheEndOfAlreadyPresentSession(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(9,0,0));
            session.setEndOfSession(LocalTime.of(11,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_addingSessionToRoom_and_sessionStartsBeforeTheStartAndEndsAfterTheEndOfAlreadyPresentSession(){
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            roomOne.addSession(sessionTwo);

            session.setStartOfSession(LocalTime.of(9,0,0));
            session.setEndOfSession(LocalTime.of(13,0,0));
            Exception exception  = assertThrows(BadRequestApiRequestException.class,()-> roomServiceImplementation.canAddSessionToRoom(roomOne,session));
            assertEquals("Yoga session time period is already occupied.",exception.getMessage());
        }
        @Test
        void should_returnTrue_when_addingSessionToRoom_and_sessionStartsAtAlreadyPresentSessionsEndAndSessionEndsAtAnotherPresentSessionsStart(){
            YogaSession sessionThree = new YogaSession();
            sessionTwo.setStartOfSession(LocalTime.of(10,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));

            sessionThree.setStartOfSession(LocalTime.of(14,0,0));
            sessionThree.setEndOfSession(LocalTime.of(15,0,0));
            sessionThree.setDate(today.plusDays(15));
            roomOne.addSession(sessionTwo);
            roomOne.addSession(sessionThree);

            session.setStartOfSession(LocalTime.of(12,0,0));
            session.setEndOfSession(LocalTime.of(14,0,0));
            assertTrue(roomServiceImplementation.canAddSessionToRoom(roomOne,session));
        }
    }

    @Nested
    class CreateARoom{
        @Test
        public void should_setCorrectOpeningAndClosingTime_when_creatingARoom_and_settingCorrectOpeningAndClosingTime(){
            roomOne = roomServiceImplementation.createARoom(today,min,max, RoomType.AIR_ROOM);
            assertAll(
                    ()-> assertEquals(max, roomOne.getClosingHours()),
                    ()-> assertEquals(min,roomOne.getOpeningHours())
            );
        }
        @RepeatedTest(3)
        public void should_throwException400BadRequest_when_creatingARoom_and_settingIncorrectClosingTime(RepetitionInfo repetitionInfo){
            LocalTime closingTimeToUse = null;
            LocalTime maxClosingTime = max;
            LocalTime openingTime = LocalTime.of(11,0,0);

            if (repetitionInfo.getCurrentRepetition() == 1){
                closingTimeToUse = maxClosingTime.plusMinutes(30);
            }else if (repetitionInfo.getCurrentRepetition() == 2){
                closingTimeToUse = openingTime;
            }else if (repetitionInfo.getCurrentRepetition() == 3){
                closingTimeToUse = openingTime.minusHours(1);
            }
            LocalTime closingTime = closingTimeToUse;

            Exception exception = assertThrows(BadRequestApiRequestException.class,()->roomServiceImplementation.createARoom(today,openingTime,closingTime, RoomType.EARTH_ROOM));

            if (repetitionInfo.getCurrentRepetition() == 1){
                assertEquals("Cannot set closing hours after " + maxClosingTime, exception.getMessage());
            }else if (repetitionInfo.getCurrentRepetition() == 2){
                assertEquals("Opening hours and closing hours can't be the same.", exception.getMessage());
            }else if (repetitionInfo.getCurrentRepetition() == 3){
                assertEquals("Cannot set closing hours before opening hours.", exception.getMessage());
            }
        }


        @RepeatedTest(3)
        public void should_throwException400BadRequest_when_creatingARoom_and_settingIncorrectOpeningTime(RepetitionInfo repetitionInfo){
            LocalTime openingTimeToUse = null;
            LocalTime closingTime = LocalTime.of(22,0,0);

            if (repetitionInfo.getCurrentRepetition() == 1){
                openingTimeToUse = min.minusMinutes(30);
            }else if (repetitionInfo.getCurrentRepetition() == 2){
                openingTimeToUse = closingTime;
            }else if (repetitionInfo.getCurrentRepetition() == 3){
                openingTimeToUse = closingTime.plusHours(1);
            }
            LocalTime openingTime = openingTimeToUse;

            Exception exception = assertThrows(BadRequestApiRequestException.class,()->roomServiceImplementation.createARoom(today,openingTime,closingTime, RoomType.EARTH_ROOM));

            if (repetitionInfo.getCurrentRepetition() == 1){
                assertEquals("Cannot set opening hours before " + min,exception.getMessage());
            }else if (repetitionInfo.getCurrentRepetition() == 2){
                assertEquals("Opening hours and closing hours can't be the same.", exception.getMessage());
            }else if (repetitionInfo.getCurrentRepetition() == 3){
                assertEquals("Cannot set opening hours after closing hours.", exception.getMessage());
            }
        }

        @Test
        public void should_setCorrectDate_when_creatingARoom_and_settingDateInTheFuture(){
            Room room = roomServiceImplementation.createARoom(today.plusDays(1)
                    ,roomOne.getOpeningHours(),roomOne.getClosingHours(),roomOne.getRoomType());
            assertEquals(today.plusDays(1), room.getDate());
        }
        @Test
        public void should_setCurrentDate_when_creatingARoom_and_settingCurrentDate(){
            Room room = roomServiceImplementation.createARoom(today
                    ,roomOne.getOpeningHours(),roomOne.getClosingHours(),roomOne.getRoomType());
            assertEquals(today, room.getDate());
        }

        @Test
        void should_throwExceptionBadRequest400WithMessage_when_creatingARoom_and_creatingRoomWithDateInThePast(){
            Exception exception = assertThrows(BadRequestApiRequestException.class
                    , ()-> roomServiceImplementation.createARoom(today.minusDays(1),roomOne.getOpeningHours(),roomOne.getClosingHours()
                    ,roomOne.getRoomType()));
            assertEquals("Date cannot be before current date.",exception.getMessage());
        }
    }


    @Test
    public void should_returnTrue_when_checkingIfAddingSessionToRoomPossible_and_itsPossibleToAddSessionToRoom(){
        session.setStartOfSession(roomOne.getOpeningHours());
        session.setEndOfSession(session.getStartOfSession().plusHours(1));
        assertTrue(roomServiceImplementation.canAddSessionToRoom(roomOne,session));
    }

    @Test
    public void should_removeYogaSessionFromRoom_when_removingSessionFromRoom_and_roomContainsSession(){
        Room room = new Room();
        YogaSession session = new YogaSession();
        room.addSession(session);
        session.setRoom(room);

        assertTrue(roomServiceImplementation.removeSessionFromRoom(room,session));
    }
    @Test
    public void should_returnFalse_when_removingSessionFromRoom_and_roomDoesntContainSession(){
        Room room = new Room();
        YogaSession session = new YogaSession();

        assertFalse(roomServiceImplementation.removeSessionFromRoom(room,session));
    }


}
