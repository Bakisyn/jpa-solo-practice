package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.RoomRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.RoomService;
import dev.milan.jpasolopractice.service.RoomServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
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
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
            when(roomServiceImpl.createARoom(any(),any(),any(),any())).thenReturn(roomOne);
            when(roomRepository.save(any())).thenReturn(null);

            assertEquals(roomOne, roomService.createARoom(LocalDate.now().toString(), LocalTime.of(5,0,0).toString(),LocalTime.of(20,0,0).toString(), YogaRooms.AIR_ROOM.name()));
        }

        @Test
        void should_throwException409ConflictWithMessage_When_RoomAlreadyExists(){
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(new Room());
           Exception exception =  assertThrows(ConflictApiRequestException.class, () -> roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name()));
           assertEquals("Room id:" + roomOne.getId() + " already exists.", exception.getMessage());
        }
        @Test
        void should_saveRoom_when_roomNotExists(){
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
            roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name());
            verify(roomRepository,times(1)).save(any());
        }
        @Test
        void should_testFormattingOfIncomingData(){
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
            roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name());
            verify(roomServiceImpl,times(1)).checkDateFormat(any());
            verify(roomServiceImpl,times(1)).checkRoomTypeFormat(any());
            verify(roomServiceImpl,times(2)).checkTimeFormat(any());
        }
    }
    @Nested
    class AddSessionToRoom{
        @Test
        void should_returnRoom_when_roomExistsAndSessionNotExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(null);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
            when(roomServiceImpl.addSessionToRoom(any(),eq(session))).thenReturn(true);
            Room roomReturned = roomService.addSessionToRoom(roomOne,session);
            assumeTrue(roomReturned != null);
            assertAll(
                    ()-> assertEquals(roomOne.getOpeningHours(), roomReturned.getOpeningHours()),
                    ()-> assertEquals(roomOne.getClosingHours(),roomReturned.getClosingHours()),
                    ()-> assertEquals(roomOne.getRoomType(),roomReturned.getRoomType()),
                    ()-> assertEquals(roomOne.getTotalCapacity(),roomReturned.getTotalCapacity())
            );
        }
        @Test
        void should_saveRoomAndSession_when_roomExistsAndSessionNotExist(){
            ArgumentCaptor<YogaSession> yogaCaptor = ArgumentCaptor.forClass(YogaSession.class);
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);

            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(null);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
            when(roomServiceImpl.addSessionToRoom(any(),eq(session))).thenReturn(true);

            Room roomReturned = roomService.addSessionToRoom(roomOne,session);

            assumeTrue(roomReturned != null);

            verify(yogaSessionRepository,times(1)).save(yogaCaptor.capture());
            verify(roomRepository,times(1)).save(roomCaptor.capture());

            Room fromSaved = roomCaptor.getValue();
            YogaSession fromSavedSession = yogaCaptor.getValue();
            assertAll(
                    ()-> assertEquals(roomReturned.getRoomType(),fromSaved.getRoomType()),
                    ()-> assertEquals(roomReturned.getDate(), fromSaved.getDate()),
                    ()-> assertEquals(roomReturned.getOpeningHours(), fromSaved.getOpeningHours()),
                    ()-> assertEquals(session,fromSavedSession)
            );
        }

        @Test
        void should_returnNull_when_roomNotExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(null);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
            assertNull(roomService.addSessionToRoom(roomOne,session));
        }

        @Test
        void should_returnNull_when_roomAndSessionExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(session);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
            assertNull(roomService.addSessionToRoom(roomOne,session));
        }
    }
    @Nested
    class GettingSessionsFromRooms{
    @Test
    void should_returnSessionsList_when_roomNotNull(){
        List<YogaSession> yogaSessions = new ArrayList<>();
        yogaSessions.add(new YogaSession());
        yogaSessions.add(new YogaSession());
        when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
        when(roomServiceImpl.getSingleRoomSessionsInADay(roomOne)).thenReturn(yogaSessions);

        assertEquals(yogaSessions, roomService.getSingleRoomSessionsInADay(roomOne.getRoomType(),LocalDate.now()));
        verify(roomServiceImpl, times(1)).getSingleRoomSessionsInADay(roomOne);
    }
    @Test
    void should_returnNull_when_roomIsNullAndSearchingRoomByNameAndDate(){
        when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
        assertNull(roomService.getSingleRoomSessionsInADay(roomOne.getRoomType(),LocalDate.now()));
    }


    @Test
    void should_returnSessionsListForType_when_roomNotNull(){
        List<YogaSession> yogaSessions = new ArrayList<>();
        yogaSessions.add(new YogaSession());
        yogaSessions.add(new YogaSession());
        List<Room> roomList = new ArrayList<>();
        roomList.add(roomOne);
        roomList.add(roomTwo);
        when(roomRepository.findAllRoomsByDate(any())).thenReturn(roomList);
        when(roomServiceImpl.getAllRoomsSessionsInADay(any())).thenReturn(yogaSessions);

        assertEquals(yogaSessions, roomService.getAllRoomsSessionsInADay(LocalDate.now()));
        verify(roomServiceImpl, times(1)).getAllRoomsSessionsInADay(roomList);
    }

    @Test
    void should_returnNull_when_roomIsNullAndSearchingAllRoomsByDate(){
        when(roomRepository.findAllRoomsByDate(any())).thenReturn(null);

        assertNull(roomService.getAllRoomsSessionsInADay(LocalDate.now()));
        verify(roomServiceImpl, never()).getAllRoomsSessionsInADay(any());
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
        void should_removeYogaSessionFromRoom_when_roomContainsSession(){
            roomOne.addSession(session);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomServiceImpl.removeSessionFromRoom(any(),any())).thenReturn(true);
            when(roomRepository.save(any())).thenReturn(null);
            when(yogaSessionRepository.save(any())).thenReturn(null);

            assertTrue(roomService.removeSessionFromRoom(12,53));
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
