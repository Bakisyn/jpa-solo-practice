package dev.milan.jpasolopractice.yogasession.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.room.RoomRepository;
import dev.milan.jpasolopractice.room.RoomService;
import dev.milan.jpasolopractice.room.util.RoomUtil;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.YogaSessionRepository;
import dev.milan.jpasolopractice.yogasession.YogaSessionService;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@SpringBootTest
public class YogaPatcherTest {
    @MockBean
    private YogaSessionService sessionService;
    @MockBean
    private YogaSessionUtil yogaSessionUtil;
    @MockBean
    private YogaSessionRepository yogaSessionRepository;

    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private SessionInputChecker sessionInputChecker;
    @MockBean
    private RoomUtil roomUtil;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private RoomService roomService;
    @Autowired
    private Patcher<YogaSession> patcher;

    private LocalDate date;
    private RoomType yogaRoomType;
    private LocalTime startTime;
    private int duration;
    private YogaSession session;
    private Person personOne;
    private final LocalDate today = LocalDate.now();

    private Room roomOne;
    private Room roomTwo;
    private List<YogaSession> sessionList;
    private JsonPatch jsonPatch;
    private String updatePatchInfo;

    @BeforeEach
    void init()  {
        date = today.plus(1, ChronoUnit.DAYS);

        roomOne = new Room();
        roomOne.setDate(today.plus(1,ChronoUnit.DAYS));
        roomOne.setOpeningHours(LocalTime.of(5,0,0));
        roomOne.setClosingHours(LocalTime.of(20,0,0));
        roomOne.setRoomType(RoomType.AIR_ROOM);
        roomOne.setTotalCapacity(RoomType.AIR_ROOM.getMaxCapacity());

        roomTwo = new Room();
        roomTwo.setRoomType(RoomType.EARTH_ROOM);
        roomTwo.setDate(today.plus(1,ChronoUnit.DAYS));
        roomTwo.setOpeningHours(LocalTime.of(9,0,0));
        roomTwo.setClosingHours(LocalTime.of(23,0,0));
        roomTwo.setTotalCapacity(RoomType.EARTH_ROOM.getMaxCapacity());

        startTime = LocalTime.of(10,0,0);
        duration = 60;

        session = new YogaSession();
        session.setRoom(roomOne);
        session.setStartOfSession(LocalTime.of(8,0,0));
        session.setDuration(45);
        session.setEndOfSession(LocalTime.of(8,45,0));
        session.setDate(today);

        personOne = new Person();
        personOne.setEmail("example@hotmail.com");
        personOne.setAge(33);
        personOne.setName("Badji");
        personOne.setName("Kukumber");

        yogaRoomType = RoomType.AIR_ROOM;


        sessionList = new ArrayList<>();
        sessionList.add(session);


        updatePatchInfo = "[\n" +
                "    {\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"},\n" +
                "    {\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},\n" +
                "    {\"op\":\"replace\",\"path\":\"/startOfSession\", \"value\":\"13:00:00\"},\n" +
                "    {\"op\":\"replace\",\"path\":\"/duration\", \"value\":\"60\"}\n" +
                "]";

    }

    @Nested
    class PatchingASession{

        @Test
        void should_updateSession_when_updatingSessionWithDateAndRoomType_and_roomOfSameTypeWithFreeSpaceAvailable() throws IOException, JsonPatchException {
            session.addMember(personOne);
            personOne.addSession(session);
            String myPatchInfo = updatePatchInfo.replace("2025-05-22", LocalDate.now().plusDays(1).toString());
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched = jsonPatch.apply(mapper.convertValue(session, JsonNode.class));
            YogaSession patchedSession =  mapper.treeToValue(patched, YogaSession.class);

            when(sessionInputChecker.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(roomService.findSingleRoomByDateAndType(any(),any())).thenReturn(roomTwo);
            when(yogaSessionUtil.createAYogaSession(patchedSession.getDate(), patchedSession.getRoomType()
                    ,patchedSession.getStartOfSession(), patchedSession.getDuration())).thenReturn(patchedSession);
            when(roomUtil.canAddSessionToRoom(roomTwo, patchedSession)).thenReturn(true);
            when(roomService.removeSessionFromRoom(session.getRoom().getId(), session.getId())).thenReturn(roomOne);
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));


            assertAll(
                    ()-> assertEquals(roomTwo.getRoomType(),(patchedSession.getRoomType())),
                    ()-> assertEquals(patchedSession, patcher.patch(jsonPatch, session))
            );

            verify(yogaSessionRepository,times(2)).save(patchedSession);
            verify(roomRepository,times(1)).save(roomTwo);
        }
        @Test
        void should_notUpdateSession_when_updatingSessionWithDateAndRoomType_and_roomOfSameTypeWithAvailableButCantAdd() throws IOException, JsonPatchException {

            String myPatchInfo = updatePatchInfo.replace("2025-05-22",LocalDate.now().plusDays(1).toString());
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched = jsonPatch.apply(mapper.convertValue(session, JsonNode.class));
            YogaSession patchedSession =  mapper.treeToValue(patched, YogaSession.class);

            when(sessionInputChecker.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(roomService.findSingleRoomByDateAndType(any(),any())).thenReturn(roomTwo);
            when(yogaSessionUtil.createAYogaSession(patchedSession.getDate(), patchedSession.getRoomType()
                    ,patchedSession.getStartOfSession(), patchedSession.getDuration())).thenReturn(patchedSession);
            when(roomUtil.canAddSessionToRoom(roomTwo, patchedSession)).thenReturn(false);
            when(roomService.removeSessionFromRoom(session.getRoom().getId(), session.getId())).thenReturn(roomOne);
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));


            assertAll(
                    ()-> assertEquals(roomTwo.getRoomType(),(patchedSession.getRoomType())),
                    ()-> assertNull(patcher.patch(jsonPatch, session))
            );

            verify(yogaSessionRepository,never()).save(patchedSession);
            verify(roomRepository,never()).save(roomTwo);
        }

        @Test
        void should_updateSession_when_updatingSessionWithDateAndRoomType_and_sessionNotInRoom() throws IOException, JsonPatchException {
            session.setRoom(null);
            String myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"}]";
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched = jsonPatch.apply(mapper.convertValue(session, JsonNode.class));
            YogaSession patchedSession =  mapper.treeToValue(patched, YogaSession.class);
            when(sessionInputChecker.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(sessionInputChecker.checkNumberFormat("" + patchedSession.getDuration())).thenReturn(patchedSession.getDuration());
            when(sessionInputChecker.checkDateFormat(any())).thenReturn(patchedSession.getDate());
            when(sessionInputChecker.checkRoomTypeFormat(any())).thenReturn(patchedSession.getRoomType());
            when(yogaSessionUtil.createAYogaSession(any(), any()
                    ,any(), anyInt())).thenReturn(patchedSession);
            when(yogaSessionRepository.save(patchedSession)).thenReturn(patchedSession);

            assertEquals(patchedSession,patcher.patch(jsonPatch, session));
            verify(yogaSessionRepository,times(1)).save(patchedSession);

        }
        @Test
        void should_updateSession_when_updatingSessionWithStartTimeAndDuration_and_sessionNotInRoom() throws IOException, JsonPatchException {
            session.setRoom(null);
            session.setRoomType(RoomType.AIR_ROOM);
            String myPatchInfo = "[{\"op\":\"replace\",\"path\":\"/startOfSession\", \"value\":\"14:00:00\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/duration\", \"value\":\"45\"}]";
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched = jsonPatch.apply(mapper.convertValue(session, JsonNode.class));
            YogaSession patchedSession =  mapper.treeToValue(patched, YogaSession.class);
            when(sessionInputChecker.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(sessionInputChecker.checkNumberFormat("" + patchedSession.getDuration())).thenReturn(patchedSession.getDuration());
            when(sessionInputChecker.checkDateFormat(any())).thenReturn(patchedSession.getDate());
            when(sessionInputChecker.checkRoomTypeFormat(any())).thenReturn(patchedSession.getRoomType());
            when(yogaSessionUtil.createAYogaSession(any(), any()
                    ,any(), anyInt())).thenReturn(patchedSession);
            when(yogaSessionRepository.save(patchedSession)).thenReturn(patchedSession);

            assertEquals(patchedSession,patcher.patch(jsonPatch, session));
            verify(yogaSessionRepository,times(1)).save(patchedSession);

        }

        @RepeatedTest(7)
        void should_throwException400BadRequest_when_updatingSession_and_passingParametersThatShouldNotBePatched(RepetitionInfo repetitionInfo) throws IOException {

            String myPatchInfo = null;

            if (repetitionInfo.getCurrentRepetition() == 1){
                myPatchInfo = updatePatchInfo.replace("roomType","id");
                myPatchInfo = myPatchInfo.replace("EARTH_ROOM","123");
            }
            if (repetitionInfo.getCurrentRepetition() == 2){
                session.addMember(personOne);
                myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"}"
                        ,"{\"op\":\"remove\",\"path\":\"/membersAttending/" + session.getId() + "\" }");
            }
            if (repetitionInfo.getCurrentRepetition() == 3){
                session.addMember(personOne);
                myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"}"
                        ,"{\"op\":\"add\",\"path\":\"/room\", \"value\":{\"date\":\"2025-04-20\",\"type\":\"AIR_ROOM\",\"openingHours\":\"13:00:00\",\"closingHours\":\"14:00:00\"} }");
            }
            if (repetitionInfo.getCurrentRepetition() == 4){
                session.addMember(personOne);
                myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"}"
                        ,"{\"op\":\"add\",\"path\":\"/endOfSession\", \"value\":\"16:30:00\" }");
            }
            if (repetitionInfo.getCurrentRepetition() == 5){
                session.addMember(personOne);
                myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"}"
                        ,"{\"op\":\"add\",\"path\":\"/bookedSpace\", \"value\":\"45\" }");
            }
            if (repetitionInfo.getCurrentRepetition() == 6){
                session.addMember(personOne);
                myPatchInfo = updatePatchInfo.replace("{\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"}"
                        ,"{\"op\":\"add\",\"path\":\"/freeSpace\", \"value\":\"12\" }");
            }
            if (repetitionInfo.getCurrentRepetition() == 7){
                session.setBookedSpace(70);
                session.setRoomType(RoomType.AIR_ROOM);
                myPatchInfo = updatePatchInfo;
            }


            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            when(sessionInputChecker.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));

            Exception exception = assertThrows(ApiRequestException.class,()-> patcher.patch(jsonPatch, session));

            if (repetitionInfo.getCurrentRepetition() == 1){
                assertEquals(exception.getMessage(), "Patch request cannot change session id.");
            }
            if (repetitionInfo.getCurrentRepetition() == 2){
                assertEquals(exception.getMessage(), "Patch request cannot change session members.");
            }
            if (repetitionInfo.getCurrentRepetition() == 3){
                assertEquals(exception.getMessage(), "Patch request cannot directly assign a room.");
            }
            if (repetitionInfo.getCurrentRepetition() == 4){
                assertEquals(exception.getMessage(), "Patch request cannot directly set end of session. Pass start time and duration of session.");
            }
            if (repetitionInfo.getCurrentRepetition() == 5){
                assertEquals(exception.getMessage(), "Patch request cannot directly set booked space.");
            }
            if (repetitionInfo.getCurrentRepetition() == 6){
                assertEquals(exception.getMessage(), "Patch request cannot directly set free space.");
            }
            if (repetitionInfo.getCurrentRepetition() == 7){
                assertEquals(exception.getMessage(), "Cannot change room type to a type with capacity lower than number of members in yoga session.");
            }


            verify(yogaSessionRepository,never()).save(any());
            verify(roomRepository,never()).save(any());
        }
    }
}
