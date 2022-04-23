package dev.milan.jpasolopractice.room;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.room.util.RoomUtil;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSessionRepository;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.util.SessionInputChecker;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    ObjectMapper mapper;
    @Autowired
    private RoomService roomService;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private RoomUtil roomUtil;
    @MockBean
    private YogaSessionRepository yogaSessionRepository;
    @MockBean
    private SessionInputChecker sessionInputChecker;
    @MockBean
    private Patcher<Room> patcher;
    private LocalDate date;
    private RoomType roomType;
    private String dateString;
    private String roomTypeString;
    private List<Room> roomList;
    private JsonPatch jsonPatch;
    private String updatePatchInfo;
    private final LocalTime MIN_HOURS = LocalTime.of(8,0,0);
    private final LocalTime MAX_HOURS = LocalTime.of(22,0,0);



    @BeforeEach
     void init(){
        roomOne = new Room();
        roomOne.setDate(LocalDate.now());
        roomOne.setOpeningHours(MIN_HOURS.plusHours(2));
        roomOne.setClosingHours(MAX_HOURS.minusHours(2));
        roomOne.setRoomType(RoomType.AIR_ROOM);
        roomOne.setTotalCapacity(RoomType.AIR_ROOM.getMaxCapacity());

        roomTwo = new Room();
        roomTwo.setRoomType(RoomType.EARTH_ROOM);
        roomTwo.setDate(LocalDate.now());


        session = new YogaSession();
        session.setStartOfSession(MIN_HOURS.plusHours(3));
        session.setDuration(60);
        session.setEndOfSession(session.getStartOfSession().plusMinutes(session.getDuration()));
        session.setDate(LocalDate.now());

        date = LocalDate.now().plusDays(2);
        roomType = RoomType.values()[0];
        dateString = date.toString();
        roomTypeString = roomType.name();

        roomList = new ArrayList<>();
        roomList.add(roomOne);

        updatePatchInfo = "[\n" +
                "   {\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},\n" +
                "    {\"op\":\"replace\",\"path\":\"/openingHours\", \"value\":\"13:00:00\"},\n" +
                "    {\"op\":\"replace\",\"path\":\"/closingHours\", \"value\":\"22:00:00\"}\n" +
                "]";

    }
    @Nested
    class CreateARoom{
        @Test
        void should_returnRoom_when_creatingARoom_and_roomDoesntExist(){
            when(sessionInputChecker.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(sessionInputChecker.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());
            when(sessionInputChecker.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());

            when(roomRepository.findSingleRoomByDateAndType(any(),any())).thenReturn(null);
            when(roomUtil.createARoom(any(),any(),any(),any())).thenReturn(roomOne);
            when(roomRepository.save(any())).thenReturn(null);

            assertEquals(roomOne, roomService.createARoom(LocalDate.now().toString(), LocalTime.of(5,0,0).toString(),LocalTime.of(20,0,0).toString(), RoomType.AIR_ROOM.name()));
        }

        @Test
        void should_throwException409ConflictWithMessage_when_creatingARoom_and_roomAlreadyExists(){
            when(sessionInputChecker.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(sessionInputChecker.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());
            when(sessionInputChecker.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());

            when(roomRepository.findSingleRoomByDateAndType(roomOne.getDate(),roomOne.getRoomType())).thenReturn(roomOne);

            Exception exception =  assertThrows(ConflictApiRequestException.class, () -> roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name()));
           assertEquals("Room id:" + roomOne.getId() + " already exists.", exception.getMessage());
           verify(roomRepository,times(1)).findSingleRoomByDateAndType(roomOne.getDate(),roomOne.getRoomType());
        }
        @Test
        void should_saveRoom_when_creatingARoom_and_roomDoesntExist(){
            when(roomRepository.findSingleRoomByDateAndType(any(),any())).thenReturn(null);
            when(sessionInputChecker.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());
            when(sessionInputChecker.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(sessionInputChecker.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());

            roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name());
            verify(roomRepository,times(1)).save(any());
        }
        @Test
        void should_testFormattingOfIncomingData_when_creatingARoom(){
            when(roomRepository.findSingleRoomByDateAndType(any(),any())).thenReturn(null);
            when(sessionInputChecker.checkRoomTypeFormat(roomOne.getRoomType().name())).thenReturn(roomOne.getRoomType());
            when(sessionInputChecker.checkDateFormat(roomOne.getDate().toString())).thenReturn(roomOne.getDate());
            when(sessionInputChecker.checkTimeFormat(roomOne.getOpeningHours().toString())).thenReturn(roomOne.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(roomOne.getClosingHours().toString())).thenReturn(roomOne.getClosingHours());

            roomService.createARoom(roomOne.getDate().toString(), roomOne.getOpeningHours().toString(), roomOne.getClosingHours().toString(), roomOne.getRoomType().name());
            verify(sessionInputChecker,times(1)).checkDateFormat(any());
            verify(sessionInputChecker,times(1)).checkRoomTypeFormat(any());
            verify(sessionInputChecker,times(2)).checkTimeFormat(any());
        }
    }
    @Nested
    class AddSessionToRoom{

        @Test
        void should_throwException400BadRequestAndNotSaveToRepo_when_addingSessionToRoom_and_serviceMethodThrowsException(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomUtil.canAddSessionToRoom(roomOne,session)).thenThrow(new BadRequestApiRequestException(""));
            assertThrows(BadRequestApiRequestException.class, ()->roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            verify(roomRepository,never()).save(any());
            verify(yogaSessionRepository,never()).save(any());
        }
        @Test
        void should_returnYogaSessionAfterSavingToRepo_when_addingSessionToRoom_and_sessionAddedToRoom(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomUtil.canAddSessionToRoom(roomOne,session)).thenReturn(true);
            assertEquals(session, roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            verify(roomRepository,times(1)).save(roomOne);
            verify(yogaSessionRepository,times(1)).save(session);
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_addingSessionToRoom_and_roomNotFoundInRepo(){
            when(roomRepository.findById(anyInt())).thenThrow(new NotFoundApiRequestException("Room id:" + roomOne.getId() + " not found."));
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            assertEquals("Room id:" + roomOne.getId() + " not found.",exception.getMessage());
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_addingSessionToRoom_and_sessionNotFoundInRepo(){
            when(roomRepository.findById(anyInt())).thenReturn(Optional.ofNullable(roomOne));
            when(yogaSessionRepository.findById(session.getId())).thenThrow(new NotFoundApiRequestException("Yoga session id:" + session.getId() + " not found."));
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.addSessionToRoom(roomOne.getId(),session.getId()));
            assertEquals("Yoga session id:" + session.getId() + " not found.",exception.getMessage());
        }

    }

    @Nested
    class FindRoomsInRepo{
        @Test
        void should_returnRoom_when_searchingForRoomById_and_roomFoundInRepoById(){
            Optional<Room> room = Optional.of(roomOne);
            when(roomRepository.findById(any())).thenReturn(room);
            assertEquals(roomOne, roomService.findRoomById(12));
        }
        @Test
        void should_returnNull_when_searchingForRoomById_and_roomNotFoundInRepoById(){
            int id = 12;
            when(roomRepository.findById(any())).thenReturn(Optional.empty());
            Exception exception = assertThrows(ApiRequestException.class, () -> roomService.findRoomById(id));
            assertEquals("Room id:" + id + " not found.", exception.getMessage());
        }

        @Test
        void should_returnList_when_searchingRoomsBasedOnDateAndType_and_roomAndDatePresent(){
            when(sessionInputChecker.checkDateFormat(any())).thenReturn(date);
            when(sessionInputChecker.checkRoomTypeFormat(any())).thenReturn(roomType);
            when(roomRepository.findSingleRoomByDateAndType(date,roomType)).thenReturn(roomOne);
            assertEquals(roomList,roomService.findAllRoomsBasedOnParams(Optional.of(dateString),Optional.of(roomTypeString)));
            verify(roomRepository,times(1)).findSingleRoomByDateAndType(date,roomType);
        }
        @Test
        void should_returnAListOfAllRooms_when_searchingRooms_and_noOptionalsPassed(){
            when(roomRepository.findAll()).thenReturn(roomList);
            assertEquals(roomList, roomService.findAllRoomsBasedOnParams(Optional.empty(),Optional.empty()));
        }
        @Test
        void should_returnAListOfRooms_when_searchingRoomsByDate_and_datePresent(){
            when(sessionInputChecker.checkDateFormat(dateString)).thenReturn(date);
            when(roomRepository.findAllRoomsByDate(date)).thenReturn(roomList);
            assertEquals(roomList, roomService.findAllRoomsBasedOnParams(Optional.of(dateString),Optional.empty()));
        }
        @Test
        void should_returnAListOf_when_searchingRoomsByRoomType_and_roomTypePresent(){
            when(sessionInputChecker.checkRoomTypeFormat(roomTypeString)).thenReturn(roomType);
            when(roomRepository.findRoomsByRoomType(roomType)).thenReturn(roomList);
            assertEquals(roomList, roomService.findAllRoomsBasedOnParams(Optional.empty(),Optional.of(roomTypeString)));
        }

    }

    @Nested
    class RemovingAsessionFromARoom{
        @Test
        void should_returnRoomAfterSavingToRepo_when_removingSessionFromRoom_and_roomContainsSession(){
            roomOne.addSession(session);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomUtil.removeSessionFromRoom(any(),any())).thenReturn(true);
            when(roomRepository.save(any())).thenReturn(null);
            when(yogaSessionRepository.save(any())).thenReturn(null);

            assertEquals(roomOne,roomService.removeSessionFromRoom(12,53));
            verify(roomRepository, times(1)).save(roomOne);
            verify(yogaSessionRepository,times(1)).save(session);
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_removingSessionFromRoom_and_sessionNotFoundInRoom(){
            roomOne.setId(12);
            session.setId(58);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.of(roomOne));
            when(roomUtil.removeSessionFromRoom(any(),any())).thenReturn(false);

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.removeSessionFromRoom(roomOne.getId(),session.getId()));

            assertEquals("Room id:" + roomOne.getId() + " doesn't contain yoga session id:" + session.getId(),exception.getMessage());
            verify(roomRepository, never()).save(roomOne);
            verify(yogaSessionRepository,never()).save(session);
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_removingSessionFromRoom_and_yogaSessionNotFound(){
            roomOne.setId(12);
            session.setId(58);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.empty());

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.removeSessionFromRoom(roomOne.getId(),session.getId()));

            assertEquals("Yoga session with id:" + session.getId() + " doesn't exist.",exception.getMessage());
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_removingSessionFromRoom_and_roomNotFound(){
            roomOne.setId(12);
            session.setId(58);
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            when(roomRepository.findById(anyInt())).thenReturn(Optional.empty());

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.removeSessionFromRoom(roomOne.getId(),session.getId()));

            assertEquals("Room with id: " + roomOne.getId() + " doesn't exist.",exception.getMessage());
        }
    }

    @Nested
    class RemovingARoom{
        @Test
        void should_removeRoom_when_removingARoom_and_roomExists(){
            when(roomRepository.findById(anyInt())).thenReturn(Optional.ofNullable(roomOne));
            roomService.removeRoom(roomOne.getId());
            verify(roomRepository,times(1)).delete(roomOne);
        }
        @Test
        void should_setRoomToNullInAllSessions_when_removingARoom_and_roomContainsSessions(){
            roomOne.addSession(session);
            session.setRoom(roomOne);
            int num = roomOne.getSessionList().size();
            when(roomRepository.findById(anyInt())).thenReturn(Optional.ofNullable(roomOne));
            roomService.removeRoom(roomOne.getId());
            verify(roomRepository,times(1)).delete(roomOne);
            verify(yogaSessionRepository,times(num)).save(any());
            assertNull(session.getRoom());
        }
        @Test
        void should_throwException404NotFound_when_removingARoom_and_roomDoesntExist(){
            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.empty());
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.removeRoom(roomOne.getId()));
            assertEquals("Room id:" + roomOne.getId() + " not found.",exception.getMessage());
            verify(roomRepository,never()).delete(roomOne);
        }
    }

    @Nested
    class PatchingARoom{
        @Test
        void should_throwApiRequestException_when_patchingRoom_and_roomToBeUpdatedNotFound() throws IOException, JsonPatchException {
            roomOne.setId(5);
            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            when(roomRepository.findById(anyInt())).thenThrow(new NotFoundApiRequestException("Room with id:" + roomOne.getId() + " doesn't exist."));

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.patchRoom("" + roomOne,jsonPatch));
            assertEquals("Room with id:" + roomOne.getId() + " doesn't exist.",exception.getMessage());
        }
        @Test
        void should_throwException400BadRequest_when_patchingASession_and_patcherMethodThrows() throws IOException {
            roomOne.setId(5);
            when(patcher.patch(any(),any())).thenThrow(new BadRequestApiRequestException("test message"));
            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));
            when(sessionInputChecker.checkNumberFormat(""+ roomOne.getId())).thenReturn(roomOne.getId());
            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> roomService.patchRoom("" + roomOne.getId(),jsonPatch));
            assertEquals("test message",exception.getMessage());
        }
        @Test
        void should_returnModifiedRoom_when_patchingASession_and_patchedSuccessfully() throws IOException {
            roomOne.setId(5);
            when(sessionInputChecker.checkNumberFormat(""+ roomOne.getId())).thenReturn(roomOne.getId());
            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));
            when(patcher.patch(any(),any())).thenReturn(roomOne);
            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            assertEquals(roomOne,roomService.patchRoom("" + roomOne.getId(),jsonPatch));
        }
    }

//    @Nested
//    class PatchingARoom{
//        @Test
//        void should_updateRoomDate_when_updatingARoomWithADifferentDate_and_roomOfSameRoomTypeAndDateDoesntExist() throws IOException, JsonPatchException {
//            roomOne.setId(5);
//            roomOne.addSession(session);
//            session.setRoom(roomOne);
//            session.setDate(roomOne.getDate());
//            String dateToChangeTo = "2023-05-23";
//            String myPatchInfo = updatePatchInfo.replace("2025-05-22",dateToChangeTo);
//            String openingTimeToChangeTo = roomOne.getOpeningHours().toString();
//            String closingTimeToChangeTo = roomOne.getClosingHours().toString();
//            myPatchInfo = myPatchInfo.replace("13:00:00",openingTimeToChangeTo);
//            myPatchInfo = myPatchInfo.replace("22:00:00",closingTimeToChangeTo);
//            System.out.println(myPatchInfo);
//            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
//            jsonPatch = mapper.readValue(in, JsonPatch.class);
//
//            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
//            Room patchedRoom = mapper.treeToValue(patched, Room.class);
//
//            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
//            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));
//            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
//            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
//            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
//                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
//            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
//            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);
//
//            Room result = roomService.patchRoom("" + roomOne.getId(),jsonPatch);
//            assertAll(
//                    ()-> assertEquals(patchedRoom.getDate(), LocalDate.parse(dateToChangeTo)),
//                    ()-> assertEquals(patchedRoom, result),
//                    ()-> assertEquals(1, result.getSessionList().size())
//            );
//
//            session.setDate(patchedRoom.getDate());
//            verify(yogaSessionRepository,times(1)).save(session);
//            verify(roomRepository,times(1)).save(patchedRoom);
//        }
//        @Test
//        void should_updateRoomOpeningAndClosingTime_when_updatingARoomWithADifferentOpeningAndClosingTime_and_passedCorrectInfo() throws IOException, JsonPatchException {
//            roomOne.setId(5);
//            roomOne.addSession(session);
//            session.setRoom(roomOne);
//            session.setDate(roomOne.getDate());
//            String myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},","");
//            System.out.println(myPatchInfo);
//            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
//            jsonPatch = mapper.readValue(in, JsonPatch.class);
//
//            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
//            Room patchedRoom = mapper.treeToValue(patched, Room.class);
//
//            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
//            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));
//            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
//            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
//            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
//                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
//            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
//            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);
//
//            Room result = roomService.patchRoom("" + roomOne.getId(),jsonPatch);
//            assertAll(
//                    ()-> assertEquals(patchedRoom.getOpeningHours(), LocalTime.of(13,0,0)),
//                    ()-> assertEquals(patchedRoom.getClosingHours(), LocalTime.of(22,0,0)),
//                    ()-> assertEquals(patchedRoom, result)
//            );
//
//            verify(roomRepository,times(1)).save(patchedRoom);
//        }
//        @Test
//        void should_removeSessions_when_updatingARoomWithADifferentOpeningAndClosingTime_and_sessionsDontFitInTheNewTime() throws IOException, JsonPatchException {
//            roomOne.setId(5);
//            session.setRoom(roomOne);
//            YogaSession sessionOne = (YogaSession) session.clone();
//            sessionOne.setStartOfSession(LocalTime.of(15,0,0));
//            sessionOne.setEndOfSession(LocalTime.of(16,0,0));
//            YogaSession sessionTwo = (YogaSession) session.clone();
//            sessionTwo.setStartOfSession(LocalTime.of(11,0,0));
//            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
//            YogaSession sessionThree = (YogaSession) session.clone();
//            sessionThree.setStartOfSession(LocalTime.of(23,0,0));
//            sessionThree.setEndOfSession(LocalTime.of(23,45,0));
//            roomOne.addSession(sessionOne); sessionOne.setRoom(roomOne);
//            roomOne.addSession(sessionTwo); sessionTwo.setRoom(roomOne);
//            roomOne.addSession(sessionThree);   sessionThree.setRoom(roomOne);
//
//            String myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},","");
//            System.out.println(myPatchInfo);
//            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
//            jsonPatch = mapper.readValue(in, JsonPatch.class);
//
//            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
//            Room patchedRoom = mapper.treeToValue(patched, Room.class);
//
//            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
//            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));
//            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
//            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
//            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
//                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
//            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
//            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);
//
//            Room result = roomService.patchRoom("" + roomOne.getId(),jsonPatch);
//            assertAll(
//                    ()-> assertEquals(patchedRoom.getOpeningHours(), LocalTime.of(13,0,0)),
//                    ()-> assertEquals(patchedRoom.getClosingHours(), LocalTime.of(22,0,0)),
//                    ()-> assertEquals(patchedRoom, result),
//                    ()-> assertEquals(1, result.getSessionList().size()),
//                    ()-> assertEquals(sessionOne,result.getSessionList().get(0))
//            );
//            sessionTwo.setRoom(null);
//            verify(yogaSessionRepository,times(1)).save(sessionTwo);
//            sessionThree.setRoom(null);
//            verify(yogaSessionRepository,times(1)).save(sessionThree);
//            verify(roomRepository,times(1)).save(patchedRoom);
//        }
//        @Test
//        void should_removeSessionsAndChangeDate_when_updatingARoomWithADifferentOpeningAndClosingTimeAndDate_and_sessionsDontFitInTheNewTime() throws IOException, JsonPatchException {
//            roomOne.setId(5);
//            session.setRoom(roomOne);
//            YogaSession sessionOne = (YogaSession) session.clone();
//            sessionOne.setStartOfSession(LocalTime.of(15,0,0));
//            sessionOne.setEndOfSession(LocalTime.of(16,0,0));
//            YogaSession sessionTwo = (YogaSession) session.clone();
//            sessionTwo.setStartOfSession(LocalTime.of(11,0,0));
//            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
//            YogaSession sessionThree = (YogaSession) session.clone();
//            sessionThree.setStartOfSession(LocalTime.of(23,0,0));
//            sessionThree.setEndOfSession(LocalTime.of(23,45,0));
//            roomOne.addSession(sessionOne); sessionOne.setRoom(roomOne);
//            roomOne.addSession(sessionTwo); sessionTwo.setRoom(roomOne);
//            roomOne.addSession(sessionThree);   sessionThree.setRoom(roomOne);
//
//            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
//            jsonPatch = mapper.readValue(in, JsonPatch.class);
//
//            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
//            Room patchedRoom = mapper.treeToValue(patched, Room.class);
//
//            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
//            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));
//            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
//            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
//            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
//            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
//                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
//            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
//            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);
//
//            Room result = roomService.patchRoom("" + roomOne.getId(),jsonPatch);
//            assertAll(
//                    ()-> assertEquals(LocalTime.of(13,0,0),patchedRoom.getOpeningHours()),
//                    ()-> assertEquals(LocalTime.of(22,0,0),patchedRoom.getClosingHours()),
//                    ()-> assertEquals(sessionOne.getStartOfSession(),result.getSessionList().get(0).getStartOfSession()),
//                    ()-> assertEquals(sessionOne.getEndOfSession(),result.getSessionList().get(0).getEndOfSession()),
//                    ()-> assertEquals(1, result.getSessionList().size()),
//                    ()-> assertEquals(patchedRoom.getDate(), result.getSessionList().get(0).getDate())
//            );
//            sessionTwo.setRoom(null);
//            verify(yogaSessionRepository,times(1)).save(sessionTwo);
//            sessionThree.setRoom(null);
//            verify(yogaSessionRepository,times(1)).save(sessionThree);
//            sessionOne.setDate(patchedRoom.getDate());
//            verify(yogaSessionRepository,times(1)).save(sessionOne);
//            verify(roomRepository,times(1)).save(patchedRoom);
//        }
//
//        @Test
//        void should_throwApiRequestException_when_patchingRoom_and_cannotCreateARoomWithPassedData() throws IOException, JsonPatchException {
//            roomOne.setId(5);
//            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
//            jsonPatch = mapper.readValue(in, JsonPatch.class);
//
//            when(roomRepository.findById(anyInt())).thenReturn(Optional.ofNullable(roomOne));
//            when(roomUtil.createARoom(any(),any(),any(),any())).thenThrow(new BadRequestApiRequestException(""));
//
//            assertThrows(BadRequestApiRequestException.class, ()-> roomService.patchRoom("" + roomOne.getId(),jsonPatch));
//        }
//        @Test
//        void should_throwApiRequestException_when_patchingRoom_and_roomToBeUpdatedNotFound() throws IOException, JsonPatchException {
//            roomOne.setId(5);
//            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
//            jsonPatch = mapper.readValue(in, JsonPatch.class);
//
//            when(roomRepository.findById(anyInt())).thenThrow(new NotFoundApiRequestException("Room with id:" + roomOne.getId() + " doesn't exist."));
//            when(roomUtil.createARoom(any(),any(),any(),any())).thenThrow(new BadRequestApiRequestException(""));
//
//            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> roomService.patchRoom("" + roomOne.getId(),jsonPatch));
//            assertEquals("Room with id:" + roomOne.getId() + " doesn't exist.",exception.getMessage());
//        }
//        @RepeatedTest(4)
//        void should_throwException400BadRequest_when_patchingRoom_and_tryingToChangeNonAllowedParameters(RepetitionInfo repetitionInfo) throws IOException, JsonPatchException {
//            roomOne.setId(5);
//            roomOne.addSession(session);
//            String myPatchInfo = "";
//            if (repetitionInfo.getCurrentRepetition() == 1){
//                myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/id\", \"value\":\"213\"}]";
//            }else if (repetitionInfo.getCurrentRepetition() == 2){
//                myPatchInfo = "[{\"op\":\"remove\",\"path\":\"/sessionList/0\"}]";
//            }else if (repetitionInfo.getCurrentRepetition() == 3){
//                myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"FIRE_ROOM\"}]";
//            }else if (repetitionInfo.getCurrentRepetition() == 4){
//                myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/totalCapacity\", \"value\":\"45\"}]";
//            }
//            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
//            jsonPatch = mapper.readValue(in, JsonPatch.class);
//
//            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
//            Room patchedRoom = mapper.treeToValue(patched, Room.class);
//            when(roomRepository.findById(anyInt())).thenReturn(Optional.ofNullable(roomOne));
//            when(roomUtil.createARoom(any(),any(),any(),any())).thenReturn(patchedRoom);
//            Exception exception;
//            if (repetitionInfo.getCurrentRepetition() == 1){
//                System.out.println("id is " + patchedRoom.getId());
//                exception = assertThrows(BadRequestApiRequestException.class, ()-> roomService.patchRoom("" + roomOne.getId(),jsonPatch));
//                assertEquals("Patch request cannot change room id.",exception.getMessage());
//            }else if (repetitionInfo.getCurrentRepetition() == 2){
//                exception = assertThrows(BadRequestApiRequestException.class, ()-> roomService.patchRoom("" + roomOne.getId(),jsonPatch));
//                assertEquals("Patch request cannot change sessions in the room.",exception.getMessage());
//            }else if (repetitionInfo.getCurrentRepetition() == 3){
//                exception = assertThrows(BadRequestApiRequestException.class, ()-> roomService.patchRoom("" + roomOne.getId(),jsonPatch));
//                assertEquals("Patch request cannot change room type.",exception.getMessage());
//            }else if (repetitionInfo.getCurrentRepetition() == 4){
//                exception = assertThrows(BadRequestApiRequestException.class, ()-> roomService.patchRoom("" + roomOne.getId(),jsonPatch));
//                assertEquals("Patch request cannot directly set total room capacity.",exception.getMessage());
//            }
//
//        }
//    }



}
