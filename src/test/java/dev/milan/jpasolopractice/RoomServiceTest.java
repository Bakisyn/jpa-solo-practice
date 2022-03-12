package dev.milan.jpasolopractice;

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
        void should_ReturnTheRoom_When_RoomIsNotPresent(){
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
            when(roomServiceImpl.createARoom(any(),any(),any(),any())).thenReturn(roomOne);
            when(roomRepository.save(any())).thenReturn(null);

            assertEquals(roomOne, roomService.createARoom(LocalDate.now(), LocalTime.of(5,0,0),LocalTime.of(20,0,0), YogaRooms.AIR_ROOM));
        }

        @Test
        void should_ReturnNull_When_RoomIsPresent(){
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(new Room());
            assertNull(roomService.createARoom(roomOne.getDate(), roomOne.getOpeningHours(), roomOne.getClosingHours(), roomOne.getRoomType()));
        }
        @Test
        void should_SaveTheRoom(){
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
            roomService.createARoom(roomOne.getDate(), roomOne.getOpeningHours(), roomOne.getClosingHours(), roomOne.getRoomType());
            verify(roomRepository,times(1)).save(any());
        }
    }
    @Nested
    class AddSessionToRoom{
        @Test
        void should_ReturnTheRoom_When_RoomExistsAndSessionDoesnt(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(null);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
            when(roomServiceImpl.addSession(any(),eq(session))).thenReturn(true);
            Room roomReturned = roomService.addSession(roomOne,session);
            assumeTrue(roomReturned != null);
            assertAll(
                    ()-> assertEquals(roomOne.getOpeningHours(), roomReturned.getOpeningHours()),
                    ()-> assertEquals(roomOne.getClosingHours(),roomReturned.getClosingHours()),
                    ()-> assertEquals(roomOne.getRoomType(),roomReturned.getRoomType()),
                    ()-> assertEquals(roomOne.getTotalCapacity(),roomReturned.getTotalCapacity())
            );
        }
        @Test
        void should_SaveTheRoomAndSession_When_RoomExistsAndSessionDoesnt(){
            ArgumentCaptor<YogaSession> yogaCaptor = ArgumentCaptor.forClass(YogaSession.class);
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);

            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(null);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
            when(roomServiceImpl.addSession(any(),eq(session))).thenReturn(true);

            Room roomReturned = roomService.addSession(roomOne,session);

            assumeTrue(roomReturned != null);

            verify(yogaSessionRepository,times(1)).save(yogaCaptor.capture()); //za ovo moram da komentiram @PostConstruct u main  klasi
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
        void should_ReturnNull_When_RoomDoesntExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(null);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
            assertNull(roomService.addSession(roomOne,session));
        }

        @Test
        void should_ReturnNull_WhenRoomAndSessionExist(){
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoom(any(),any(),any())).thenReturn(session);
            when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
            assertNull(roomService.addSession(roomOne,session));
        }
    }
    @Nested
    class GettingSessionsFromRooms{
    @Test
    void should_ReturnAListOfSessions_When_RoomIsNotNull(){
        List<YogaSession> yogaSessions = new ArrayList<>();
        yogaSessions.add(new YogaSession());
        yogaSessions.add(new YogaSession());
        when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(roomOne);
        when(roomServiceImpl.getSingleRoomSessionsInADay(roomOne)).thenReturn(yogaSessions);

        assertEquals(yogaSessions, roomService.getSingleRoomSessionsInADay(roomOne.getRoomType(),LocalDate.now()));
        verify(roomServiceImpl, times(1)).getSingleRoomSessionsInADay(roomOne);
    }
    @Test
    void should_ReturnNull_When_RoomIsNull(){
        when(roomRepository.findRoomByNameAndDate(any(),any())).thenReturn(null);
        assertNull(roomService.getSingleRoomSessionsInADay(roomOne.getRoomType(),LocalDate.now()));
    }


    @Test
    void should_ReturnAListOfAllSessionsForType_When_RoomIsNotNull(){
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
    void should_ReturnNullInsteadOfAllRooms_When_RoomIsNull(){
        when(roomRepository.findAllRoomsByDate(any())).thenReturn(null);

        assertNull(roomService.getAllRoomsSessionsInADay(LocalDate.now()));
        verify(roomServiceImpl, never()).getAllRoomsSessionsInADay(any());
    }
}
    @Nested
    class FindRoomsInRepo{
        @Test
        void should_ReturnRoom_When_RoomIsFoundInRepoById(){
            Optional<Room> room = Optional.of(roomOne);
            when(roomRepository.findById(any())).thenReturn(room);
            assertEquals(roomOne, roomService.findRoomById(12));
        }
        @Test
        void should_ReturnNull_When_RoomIsNotFoundInRepoById(){
            when(roomRepository.findById(any())).thenReturn(Optional.empty());
            assertNull(roomService.findRoomById(12));
        }


    }

}
