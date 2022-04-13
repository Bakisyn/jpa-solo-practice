package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.RoomRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.RoomService;
import dev.milan.jpasolopractice.service.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class RoomServiceTest {
    Room roomOne;
    Room roomTwo;
    YogaSession session;

    @Autowired
    private RoomService roomService;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private RoomServiceImpl roomServiceImpl;
    @MockBean
    private YogaSessionRepository yogaSessionRepository;


    @BeforeEach
     void init(){
        roomOne = new Room();
        roomOne.setDate(LocalDate.now());
        roomOne.setOpeningHours(LocalTime.of(5,0,0));
        roomOne.setClosingHours(LocalTime.of(20,0,0));
        roomOne.setRoomType(YogaRooms.AIR_ROOM);
        roomOne.setTotalCapacity(YogaRooms.AIR_ROOM.getMaxCapacity());

        roomTwo = new Room();
        roomTwo.setRoomType(YogaRooms.EARTH_ROOM);
        roomTwo.setDate(LocalDate.now());


        session = new YogaSession();
        session.setRoom(roomOne);
        session.setStartOfSession(LocalTime.of(8,0,0));
        session.setDuration(45);
        session.setDate(LocalDate.now());
    }
    @Nested
    class CreateARoom{
        @Test
        void should_returnRoom_when_roomNotExists(){
            when(roomServiceImpl.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(roomServiceImpl.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(roomServiceImpl.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());
            when(roomServiceImpl.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());

            when(roomRepository.findRoomByDateAndRoomType(any(),any())).thenReturn(null);
            when(roomServiceImpl.createARoom(any(),any(),any(),any())).thenReturn(roomOne);
            when(roomRepository.save(any())).thenReturn(null);

            assertEquals(roomOne, roomService.createARoom(LocalDate.now().toString(), LocalTime.of(5,0,0).toString(),LocalTime.of(20,0,0).toString(), YogaRooms.AIR_ROOM.name()));
        }

        @Test
        void should_throwException409ConflictWithMessage_When_RoomAlreadyExists(){
            when(roomServiceImpl.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(roomServiceImpl.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(roomServiceImpl.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());
            when(roomServiceImpl.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());

            when(roomRepository.findRoomByDateAndRoomType(roomOne.getDate(),roomOne.getRoomType())).thenReturn(roomOne);

            Exception exception =  assertThrows(ConflictApiRequestException.class, () -> roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name()));
           assertEquals("Room id:" + roomOne.getId() + " already exists.", exception.getMessage());
           verify(roomRepository,times(1)).findRoomByDateAndRoomType(roomOne.getDate(),roomOne.getRoomType());
        }
        @Test
        void should_saveRoom_when_roomNotExists(){
            when(roomRepository.findRoomByDateAndRoomType(any(),any())).thenReturn(null);
            when(roomServiceImpl.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());
            when(roomServiceImpl.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(roomServiceImpl.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(roomServiceImpl.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());

            roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name());
            verify(roomRepository,times(1)).save(any());
        }
        @Test
        void should_testFormattingOfIncomingData(){
            when(roomRepository.findRoomByDateAndRoomType(any(),any())).thenReturn(null);
            when(roomServiceImpl.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());
            when(roomServiceImpl.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(roomServiceImpl.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(roomServiceImpl.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());

            roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name());
            verify(roomServiceImpl,times(1)).checkDateFormat(any());
            verify(roomServiceImpl,times(1)).checkRoomTypeFormat(any());
            verify(roomServiceImpl,times(2)).checkTimeFormat(any());
        }
    }
    @Nested
    class AddSessionToRoom{

        @Test
        void should_throwException400BadRequestAndNotSaveToRepo_when_serviceMethodThrowsException(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomServiceImpl.addSessionToRoom(roomOne,session)).thenThrow(new BadRequestApiRequestException(""));
            assertThrows(BadRequestApiRequestException.class, ()->roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            verify(roomRepository,never()).save(any());
            verify(yogaSessionRepository,never()).save(any());
        }
        @Test
        void should_returnYogaSessionAfterSavingToRepo_when_sessionAddedToRoom(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomServiceImpl.addSessionToRoom(roomOne,session)).thenReturn(true);
            assertEquals(session, roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            verify(roomRepository,times(1)).save(roomOne);
            verify(yogaSessionRepository,times(1)).save(session);
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_roomNotFoundInRepo(){
            when(roomRepository.findById(anyInt())).thenThrow(new NotFoundApiRequestException("Room id:" + roomOne.getId() + " not found."));
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            assertEquals("Room id:" + roomOne.getId() + " not found.",exception.getMessage());
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_sessionNotFoundInRepo(){
            when(roomRepository.findById(anyInt())).thenReturn(Optional.ofNullable(roomOne));
            when(yogaSessionRepository.findById(session.getId())).thenThrow(new NotFoundApiRequestException("Yoga session id:" + session.getId() + " not found."));
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            assertEquals("Yoga session id:" + session.getId() + " not found.",exception.getMessage());
        }

    }
    @Nested
    class GettingSessionsFromRooms{
    @Test
    void should_returnSessionsList_when_roomNotNull(){
        roomOne.addSession(session);
        when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));

        assertEquals(roomOne.getSessionList(), roomService.getSingleRoomSessionsInADay(roomOne.getId()));
    }
    @Test
    void should_throwException404NotFoundWithMessage_when_searchingSessionsInRoomAndRoomIsNull(){
        when(roomRepository.findById(roomOne.getId())).thenThrow(new NotFoundApiRequestException("Room with id:" + roomOne.getId() + " doesn't exist."));
        Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.getSingleRoomSessionsInADay(roomOne.getId()));
        assertEquals("Room with id:" + roomOne.getId() + " doesn't exist.",exception.getMessage());
    }


    @Test
    void should_returnSessionsForAllRoomsByDate_when_searchingAllSessionsFromAllRoomsByDateAndRoomsNotNull(){
        List<YogaSession> yogaSessions = new ArrayList<>();
        yogaSessions.add(session);
        yogaSessions.add(new YogaSession());

        List<Room> roomList = new ArrayList<>();
        roomList.add(roomOne);
        roomList.add(roomTwo);
        when(roomRepository.findAllRoomsByDate(any())).thenReturn(roomList);
        when(roomServiceImpl.getAllRoomsSessionsInADay(roomList)).thenReturn(yogaSessions);
        when(roomServiceImpl.checkDateFormat(LocalDate.now().toString())).thenReturn(LocalDate.now());

        assertEquals(yogaSessions, roomService.getAllRoomsSessionsInADay(LocalDate.now().toString()));
        verify(roomServiceImpl, times(1)).getAllRoomsSessionsInADay(roomList);
        verify(roomRepository,times(1)).findAllRoomsByDate(LocalDate.now());
        verify(roomServiceImpl, times(1)).checkDateFormat(any());
    }

    @Test
    void should_throwException400BadRequest_when_searchingAllSessionsFromAllRoomsByDateAndDateFormatIncorrect(){
        when(roomServiceImpl.checkDateFormat(any())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));

        Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> roomService.getAllRoomsSessionsInADay("20-2022-1"));
        assertEquals("Incorrect date. Correct format is: yyyy-mm-dd",exception.getMessage());
    }
        @Test
        void should_throwException404NotFound_when_searchingAllSessionsFromAllRoomsByDateAndNoRoomsExist(){
            when(roomServiceImpl.checkDateFormat(any())).thenReturn(LocalDate.now());
            when(roomRepository.findAllRoomsByDate(LocalDate.now())).thenReturn(null);

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.getAllRoomsSessionsInADay("2022-02-01"));
            assertEquals("No rooms found on date:" + LocalDate.now(),exception.getMessage());
        }
}
    @Nested
    class FindRoomsInRepo{
        @Test
        void should_returnRoom_when_roomFoundInRepoById(){
            Optional<Room> room = Optional.of(roomOne);
            when(roomRepository.findById(any())).thenReturn(room);
            assertEquals(roomOne, roomService.findRoomById(12));
        }
        @Test
        void should_returnNull_when_roomNotFoundInRepoById(){
            int id = 12;
            when(roomRepository.findById(any())).thenReturn(Optional.empty());
            Exception exception = assertThrows(ApiRequestException.class, () -> roomService.findRoomById(id));
            assertEquals("Room with id:" + id + " doesn't exist.", exception.getMessage());
        }

        @Test
        void should_returnRoom_when_roomWithSaidTypeAndDateExists(){
            YogaRooms type = YogaRooms.values()[0];
            LocalDate date = LocalDate.now();
            when(roomServiceImpl.checkDateFormat(date.toString())).thenReturn(date);
            when(roomServiceImpl.checkRoomTypeFormat(type.name())).thenReturn(type);
            when(roomRepository.findRoomByDateAndRoomType(date,type)).thenReturn(roomOne);

            assertEquals(roomOne, roomService.findRoomByTypeAndDate(date.toString(),type.name()));

            verify(roomRepository, times(1)).findRoomByDateAndRoomType(eq(date),eq(type));
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_roomWithSaidTypeAndDateNotExist(){
            YogaRooms type = YogaRooms.values()[0];
            LocalDate date = LocalDate.now().plusDays(2);
            when(roomServiceImpl.checkDateFormat(date.toString())).thenReturn(date);
            when(roomServiceImpl.checkRoomTypeFormat(type.name())).thenReturn(type);

            when(roomRepository.findRoomByDateAndRoomType(date,type)).thenReturn(null);
            Exception exception = assertThrows(ApiRequestException.class, ()-> roomService.findRoomByTypeAndDate(date.toString(),type.name()));

            assertEquals("Room on date:" + date + " ,of type:" + type.name() +" not found.", exception.getMessage());
        }
    }

    @Nested
    class RemovingAsessionFromARoom{
        @Test
        void should_returnRoomAfterSavingToRepo_when_removingSessionFromRoomAndRoomContainsSession(){
            roomOne.addSession(session);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomServiceImpl.removeSessionFromRoom(any(),any())).thenReturn(true);
            when(roomRepository.save(any())).thenReturn(null);
            when(yogaSessionRepository.save(any())).thenReturn(null);

            assertEquals(roomOne,roomService.removeSessionFromRoom(12,53));
            verify(roomRepository, times(1)).save(roomOne);
            verify(yogaSessionRepository,times(1)).save(session);
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_sessionNotFoundInRoom(){
            roomOne.setId(12);
            session.setId(58);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomServiceImpl.removeSessionFromRoom(any(),any())).thenReturn(false);

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.removeSessionFromRoom(roomOne.getId(),session.getId()));

            assertEquals("Room id:" + roomOne.getId() + " doesn't contain yoga session id:" + session.getId(),exception.getMessage());
            verify(roomRepository, never()).save(roomOne);
            verify(yogaSessionRepository,never()).save(session);
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_yogaSessionNotFound(){
            roomOne.setId(12);
            session.setId(58);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.empty());

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.removeSessionFromRoom(roomOne.getId(),session.getId()));

            assertEquals("Yoga session with id:" + session.getId() + " doesn't exist.",exception.getMessage());
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_roomNotFound(){
            roomOne.setId(12);
            session.setId(58);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.empty());

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.removeSessionFromRoom(roomOne.getId(),session.getId()));

            assertEquals("Room with id: " + roomOne.getId() + " doesn't exist.",exception.getMessage());
        }
    }





}
