package dev.milan.jpasolopractice.room.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.room.RoomRepository;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.YogaSessionRepository;
import dev.milan.jpasolopractice.yogasession.util.SessionInputChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class RoomPatcherTest {

    Room roomOne;
    YogaSession session;

    @Autowired
    ObjectMapper mapper;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private RoomUtil roomUtil;
    @MockBean
    private YogaSessionRepository yogaSessionRepository;
    @MockBean
    private SessionInputChecker sessionInputChecker;
    @Autowired
    private Patcher<Room> patcher;
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

        session = new YogaSession();
        session.setStartOfSession(MIN_HOURS.plusHours(3));
        session.setDuration(60);
        session.setEndOfSession(session.getStartOfSession().plusMinutes(session.getDuration()));
        session.setDate(LocalDate.now());


        roomOne.setId(5);
        roomOne.addSession(session);
        session.setRoom(roomOne);
        session.setDate(roomOne.getDate());

        updatePatchInfo = "[\n" +
                "   {\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},\n" +
                "    {\"op\":\"replace\",\"path\":\"/openingHours\", \"value\":\"13:00:00\"},\n" +
                "    {\"op\":\"replace\",\"path\":\"/closingHours\", \"value\":\"22:00:00\"}\n" +
                "]";
    }

        @Test
        void should_updateRoomDate_when_updatingARoomWithADifferentDate_and_roomOfSameRoomTypeAndDateDoesntExist() throws IOException, JsonPatchException {
            String dateToChangeTo = "2023-05-23";
            String myPatchInfo = updatePatchInfo.replace("2025-05-22",dateToChangeTo);
            String openingTimeToChangeTo = roomOne.getOpeningHours().toString();
            String closingTimeToChangeTo = roomOne.getClosingHours().toString();
            myPatchInfo = myPatchInfo.replace("13:00:00",openingTimeToChangeTo);
            myPatchInfo = myPatchInfo.replace("22:00:00",closingTimeToChangeTo);
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
            Room patchedRoom = mapper.treeToValue(patched, Room.class);

            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);

            Room result = patcher.patch(jsonPatch,roomOne);
            assertAll(
                    ()-> assertEquals(patchedRoom.getDate(), LocalDate.parse(dateToChangeTo)),
                    ()-> assertEquals(patchedRoom, result),
                    ()-> assertEquals(1, result.getSessionList().size())
            );

            session.setDate(patchedRoom.getDate());
            verify(yogaSessionRepository,times(1)).save(session);
            verify(roomRepository,times(1)).save(patchedRoom);
        }
        @Test
        void should_updateRoomOpeningAndClosingTime_when_updatingARoomWithADifferentOpeningAndClosingTime_and_passedCorrectInfo() throws IOException, JsonPatchException {

            String myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},","");
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
            Room patchedRoom = mapper.treeToValue(patched, Room.class);

            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);

            Room result = patcher.patch(jsonPatch,roomOne);
            assertAll(
                    ()-> assertEquals(patchedRoom.getOpeningHours(), LocalTime.of(13,0,0)),
                    ()-> assertEquals(patchedRoom.getClosingHours(), LocalTime.of(22,0,0)),
                    ()-> assertEquals(patchedRoom, result)
            );

            verify(roomRepository,times(1)).save(patchedRoom);
        }
        @Test
        void should_removeSessions_when_updatingARoomWithADifferentOpeningAndClosingTime_and_sessionsDontFitInTheNewTime() throws IOException, JsonPatchException {
            roomOne.setSessionList(new ArrayList<>());
            YogaSession sessionOne = (YogaSession) session.clone();
            sessionOne.setStartOfSession(LocalTime.of(15,0,0));
            sessionOne.setEndOfSession(LocalTime.of(16,0,0));
            YogaSession sessionTwo = (YogaSession) session.clone();
            sessionTwo.setStartOfSession(LocalTime.of(11,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            YogaSession sessionThree = (YogaSession) session.clone();
            sessionThree.setStartOfSession(LocalTime.of(23,0,0));
            sessionThree.setEndOfSession(LocalTime.of(23,45,0));
            roomOne.addSession(sessionOne); sessionOne.setRoom(roomOne);
            roomOne.addSession(sessionTwo); sessionTwo.setRoom(roomOne);
            roomOne.addSession(sessionThree);   sessionThree.setRoom(roomOne);

            String myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},","");
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
            Room patchedRoom = mapper.treeToValue(patched, Room.class);

            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);

            Room result = patcher.patch(jsonPatch,roomOne);
            assertAll(
                    ()-> assertEquals(patchedRoom.getOpeningHours(), LocalTime.of(13,0,0)),
                    ()-> assertEquals(patchedRoom.getClosingHours(), LocalTime.of(22,0,0)),
                    ()-> assertEquals(patchedRoom, result),
                    ()-> assertEquals(1, result.getSessionList().size()),
                    ()-> assertEquals(sessionOne,result.getSessionList().get(0))
            );
            sessionTwo.setRoom(null);
            verify(yogaSessionRepository,times(1)).save(sessionTwo);
            sessionThree.setRoom(null);
            verify(yogaSessionRepository,times(1)).save(sessionThree);
            verify(roomRepository,times(1)).save(patchedRoom);
        }
        @Test
        void should_removeSessionsAndChangeDate_when_updatingARoomWithADifferentOpeningAndClosingTimeAndDate_and_sessionsDontFitInTheNewTime() throws IOException, JsonPatchException {
            roomOne.setSessionList(new ArrayList<>());
            YogaSession sessionOne = (YogaSession) session.clone();
            sessionOne.setStartOfSession(LocalTime.of(15,0,0));
            sessionOne.setEndOfSession(LocalTime.of(16,0,0));
            YogaSession sessionTwo = (YogaSession) session.clone();
            sessionTwo.setStartOfSession(LocalTime.of(11,0,0));
            sessionTwo.setEndOfSession(LocalTime.of(12,0,0));
            YogaSession sessionThree = (YogaSession) session.clone();
            sessionThree.setStartOfSession(LocalTime.of(23,0,0));
            sessionThree.setEndOfSession(LocalTime.of(23,45,0));
            roomOne.addSession(sessionOne); sessionOne.setRoom(roomOne);
            roomOne.addSession(sessionTwo); sessionTwo.setRoom(roomOne);
            roomOne.addSession(sessionThree);   sessionThree.setRoom(roomOne);

            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
            Room patchedRoom = mapper.treeToValue(patched, Room.class);

            when(sessionInputChecker.checkNumberFormat("" + roomOne.getId())).thenReturn(roomOne.getId());
            when(sessionInputChecker.checkDateFormat(patchedRoom.getDate().toString())).thenReturn(patchedRoom.getDate());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getOpeningHours().toString())).thenReturn(patchedRoom.getOpeningHours());
            when(sessionInputChecker.checkTimeFormat(patchedRoom.getClosingHours().toString())).thenReturn(patchedRoom.getClosingHours());
            when(sessionInputChecker.checkRoomTypeFormat(patchedRoom.getRoomType().name())).thenReturn(patchedRoom.getRoomType());
            when(roomUtil.createARoom(patchedRoom.getDate(),patchedRoom.getOpeningHours()
                    ,patchedRoom.getClosingHours(),patchedRoom.getRoomType())).thenReturn(patchedRoom);
            when(roomRepository.findSingleRoomByDateAndType(patchedRoom.getDate(),patchedRoom.getRoomType())).thenReturn(null);
            when(roomRepository.save(patchedRoom)).thenReturn(patchedRoom);

            Room result = patcher.patch(jsonPatch,roomOne);
            assertAll(
                    ()-> assertEquals(LocalTime.of(13,0,0),patchedRoom.getOpeningHours()),
                    ()-> assertEquals(LocalTime.of(22,0,0),patchedRoom.getClosingHours()),
                    ()-> assertEquals(sessionOne.getStartOfSession(),result.getSessionList().get(0).getStartOfSession()),
                    ()-> assertEquals(sessionOne.getEndOfSession(),result.getSessionList().get(0).getEndOfSession()),
                    ()-> assertEquals(1, result.getSessionList().size()),
                    ()-> assertEquals(patchedRoom.getDate(), result.getSessionList().get(0).getDate())
            );
            sessionTwo.setRoom(null);
            verify(yogaSessionRepository,times(1)).save(sessionTwo);
            sessionThree.setRoom(null);
            verify(yogaSessionRepository,times(1)).save(sessionThree);
            sessionOne.setDate(patchedRoom.getDate());
            verify(yogaSessionRepository,times(1)).save(sessionOne);
            verify(roomRepository,times(1)).save(patchedRoom);
        }

        @Test
        void should_throwApiRequestException_when_patchingRoom_and_cannotCreateARoomWithPassedData() throws IOException, JsonPatchException {
            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            when(roomUtil.createARoom(any(),any(),any(),any())).thenThrow(new BadRequestApiRequestException(""));

            assertThrows(BadRequestApiRequestException.class, ()-> patcher.patch(jsonPatch,roomOne));
        }

        @RepeatedTest(4)
        void should_throwException400BadRequest_when_patchingRoom_and_tryingToChangeNonAllowedParameters(RepetitionInfo repetitionInfo) throws IOException, JsonPatchException {
            roomOne.setId(5);
            roomOne.addSession(session);
            String myPatchInfo = "";
            if (repetitionInfo.getCurrentRepetition() == 1){
                myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/id\", \"value\":\"213\"}]";
            }else if (repetitionInfo.getCurrentRepetition() == 2){
                myPatchInfo = "[{\"op\":\"remove\",\"path\":\"/sessionList/0\"}]";
            }else if (repetitionInfo.getCurrentRepetition() == 3){
                myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"FIRE_ROOM\"}]";
            }else if (repetitionInfo.getCurrentRepetition() == 4){
                myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/totalCapacity\", \"value\":\"45\"}]";
            }
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched  = jsonPatch.apply(mapper.convertValue(roomOne, JsonNode.class));
            Room patchedRoom = mapper.treeToValue(patched, Room.class);
            when(roomUtil.createARoom(any(),any(),any(),any())).thenReturn(patchedRoom);
            Exception exception;
            if (repetitionInfo.getCurrentRepetition() == 1){
                System.out.println("id is " + patchedRoom.getId());
                exception = assertThrows(BadRequestApiRequestException.class, ()-> patcher.patch(jsonPatch,roomOne));
                assertEquals("Patch request cannot change room id.",exception.getMessage());
            }else if (repetitionInfo.getCurrentRepetition() == 2){
                exception = assertThrows(BadRequestApiRequestException.class, ()-> patcher.patch(jsonPatch,roomOne));
                assertEquals("Patch request cannot change sessions in the room.",exception.getMessage());
            }else if (repetitionInfo.getCurrentRepetition() == 3){
                exception = assertThrows(BadRequestApiRequestException.class, ()-> patcher.patch(jsonPatch,roomOne));
                assertEquals("Patch request cannot change room type.",exception.getMessage());
            }else if (repetitionInfo.getCurrentRepetition() == 4){
                exception = assertThrows(BadRequestApiRequestException.class, ()-> patcher.patch(jsonPatch,roomOne));
                assertEquals("Patch request cannot directly set total room capacity.",exception.getMessage());
            }

        }
}
