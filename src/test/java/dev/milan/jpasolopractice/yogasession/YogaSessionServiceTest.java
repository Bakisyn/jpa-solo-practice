package dev.milan.jpasolopractice.yogasession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.person.PersonRepository;
import dev.milan.jpasolopractice.room.RoomRepository;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.roomtype.RoomType;
import dev.milan.jpasolopractice.room.RoomService;
import dev.milan.jpasolopractice.room.RoomServiceUtil;
import dev.milan.jpasolopractice.yogasession.util.SessionInputChecker;
import dev.milan.jpasolopractice.yogasession.util.YogaSessionUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class YogaSessionServiceTest {
    @Autowired
    private YogaSessionService sessionService;
    @MockBean
    private YogaSessionUtil yogaSessionUtil;
    @MockBean
    private YogaSessionRepository yogaSessionRepository;
    @MockBean
    private PersonRepository personRepository;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private SessionInputChecker sessionInputChecker;
    @MockBean
    private RoomServiceUtil roomServiceUtil;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private RoomService roomService;

    private LocalDate date;
    private RoomType yogaRoomType;
    private LocalTime startTime;
    private int duration;
    private YogaSession session;
    private Person personOne;
    private final LocalDate today = LocalDate.now();
    private String dateString;
    private String roomTypeString;
    private String startTimeString;
    private String durationString;
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

        dateString = date.toString();
        roomTypeString = yogaRoomType.name();
        startTimeString = startTime.toString();
        durationString = "" + duration;

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
    class CreateYogaSession{
        @Test
        void should_createYogaSession_when_creatingYogaSession_and_sessionInfoCorrect() throws NotFoundApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
            when(sessionInputChecker.checkDateFormat(any())).thenReturn(date);
            when(sessionInputChecker.checkNumberFormat(anyString())).thenReturn(duration);
            when(sessionInputChecker.checkRoomTypeFormat(any())).thenReturn(yogaRoomType);
            when(sessionInputChecker.checkTimeFormat(startTimeString)).thenReturn(startTime);
            when(yogaSessionUtil.createAYogaSession(date,yogaRoomType,startTime,duration)).thenReturn(session);

            assertEquals(session, sessionService.createAYogaSession(dateString, roomTypeString,startTimeString,durationString));
        }
        @Test
        void should_throwException400BadRequest_when_creatingYogaSession_and_RoomTypeIsNull() throws BadRequestApiRequestException {
            when(sessionInputChecker.checkRoomTypeFormat(any())).thenThrow(new BadRequestApiRequestException(""));
            assertThrows(BadRequestApiRequestException.class,() ->  sessionService.createAYogaSession(dateString, null,startTimeString,durationString));
        }
        @Test
        void should_throwException400BadRequest_when_creatingSession_and_dateIsNull() throws BadRequestApiRequestException {
            when(sessionInputChecker.checkDateFormat(any())).thenThrow(new BadRequestApiRequestException(""));
            assertThrows(BadRequestApiRequestException.class,() ->  sessionService.createAYogaSession(null, roomTypeString,startTimeString,durationString));
        }
        @Test
        void should_saveSessionInRepo_when_creatingYogaSession_and_sessionInfoCorrect() throws NotFoundApiRequestException {
            ArgumentCaptor<YogaSession> sessionCaptor = ArgumentCaptor.forClass(YogaSession.class);
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
            when(yogaSessionUtil.createAYogaSession(any(),any(),any(),anyInt())).thenReturn(session);

            sessionService.createAYogaSession(dateString, roomTypeString,startTimeString,durationString);

            verify(yogaSessionRepository,times(1)).save(sessionCaptor.capture());
            assertEquals(session, sessionCaptor.getValue());
        }

        @Test
        void should_notSaveSession_when_creatingYogaSession_and_sessionInfoIncorrect() {
            try{
                when(yogaSessionRepository.findYogaSessionByDateAndStartOfSession(any(),any())).thenReturn(null);
                when(yogaSessionUtil.createAYogaSession(any(),any(),any(),anyInt())).thenThrow(new NotFoundApiRequestException(""));
                sessionService.createAYogaSession(dateString, roomTypeString,startTimeString,durationString);
            }catch (ApiRequestException e){

            }finally {
                verify(yogaSessionRepository,never()).save(any());
            }
        }

        @Test
        void should_throwException400BadRequest_when_creatingYogaSession_and_sessionInfoIncorrect() throws BadRequestApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(any(),any(),any())).thenReturn(null);
            when(yogaSessionUtil.createAYogaSession(any(),any(),any(),anyInt())).thenThrow(new BadRequestApiRequestException(""));
            Executable executable = () -> sessionService.createAYogaSession(dateString,roomTypeString,startTimeString,durationString);
            assertThrows(BadRequestApiRequestException.class, executable);
        }

        @Test
        void should_throwException409ConflictWithMessage_when_creatingYogaSession_and_sessionAlreadyExists() throws ConflictApiRequestException {
            when(yogaSessionRepository.findYogaSessionByDateAndStartOfSessionAndRoomType(any(),any(),any())).thenReturn(session);
            Exception exception = assertThrows(ConflictApiRequestException.class,()-> sessionService.createAYogaSession(dateString,roomTypeString,startTimeString,durationString));
            assertEquals("Yoga session with same date,start time and room type already exists.",exception.getMessage());
        }
    }
    @Nested
    class AddMemberToYogaSession {
        @Test
        void should_addYogaSessionToPerson_when_addingPersonToSession_and_sessionDoesntContainPerson(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            when(yogaSessionUtil.addMember(personOne,session)).thenReturn(true);

            sessionService.addMemberToYogaSession(session.getId(),personOne.getId());

            verify(personRepository,times(1)).save(personOne);
            verify(yogaSessionRepository,times(1)).save(session);
        }
        @Test
        void should_throwException404NotFound_when_addingPersonToSession_and_personDoesntExist(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.empty());
            Exception exception = assertThrows(NotFoundApiRequestException.class, () ->sessionService.addMemberToYogaSession(session.getId(),personOne.getId()));
            assertEquals("Person id:" + personOne.getId() + " couldn't be found.",exception.getMessage());
        }

        @Test
        void should_throwException404NotFound_when_addingPersonToSession_and_sessionDoesntExist(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.empty());
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            Exception exception = assertThrows(NotFoundApiRequestException.class, () ->sessionService.addMemberToYogaSession(session.getId(),personOne.getId()));
            assertEquals("Yoga session id:" + session.getId() +  " not found.",exception.getMessage());
        }


    }
    @Nested
    class RemoveMemberFromSession{

        @Test
        void should_returnTrue_when_removingPersonFromSession_and_personAndSessionExist_and_personContainsSession(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(yogaSessionUtil.removeMember(personOne,session)).thenReturn(true);

            assertTrue(sessionService.removeMemberFromYogaSession(personOne.getId(),session.getId()));
            verify(yogaSessionRepository,times(1)).save(session);
            verify(personRepository,times(1)).save(personOne);
        }

        @Test
        void should_throwException404NotFound_when_removingPersonFromSession_and_sessionDoesntContainPerson(){
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(yogaSessionUtil.removeMember(personOne,session)).thenThrow(new NotFoundApiRequestException("Person id:" + personOne.getId() + " not found in session id:" + session.getId()));

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.removeMemberFromYogaSession(personOne.getId(),session.getId()));
            assertEquals("Person id:" + personOne.getId() + " not found in session id:" + session.getId(), exception.getMessage());

            verify(yogaSessionRepository,times(0)).save(any());
            verify(personRepository,times(0)).save(any());
        }


    }

    @Test
    void should_passCorrectEndOfSessionValuesFromImpl(){
        when(yogaSessionUtil.getEndOfSession(session)).thenReturn(session.getEndOfSession());
        assertEquals(session.getEndOfSession(), sessionService.getEndOfSession(session));
    }

    @Nested
    class SearchingForSessions{

        @Test
        void should_returnSessionList_when_searchingAllSessions(){
            List<YogaSession> sessionList = new ArrayList<>();
            sessionList.add(session);
            when(yogaSessionRepository.findAll()).thenReturn(sessionList);
            assertEquals(sessionList, sessionService.findAllSessions());
        }


        @Test
        void should_throwException404NotFoundWithMessage_when_searchingForYogaSessionById_and_sessionNotFound(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.empty());
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.findYogaSessionById(session.getId()));

            assertEquals("Yoga session id:" + session.getId() + " not found.",exception.getMessage());
        }

        @Test
        void should_returnSession_when_searchingForSessionById_and_sessionIsFound(){
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));

            assertEquals(session, sessionService.findYogaSessionById(12));
        }


        @Test
        void should_returnSessionsForAllRooms_when_searchingAllSessionsFromAllRoomsByDate_and_roomsNotNull(){
            List<YogaSession> yogaSessions = new ArrayList<>();
            yogaSessions.add(session);
            yogaSessions.add(new YogaSession());

            List<Room> roomList = new ArrayList<>();
            roomOne.setSessionList(yogaSessions);
            roomList.add(roomOne);
            when(roomRepository.findAllRoomsByDate(any())).thenReturn(roomList);
            when(sessionInputChecker.checkDateFormat(LocalDate.now().toString())).thenReturn(LocalDate.now());

            assertEquals(yogaSessions, sessionService.findAllSessionsInAllRoomsByDate(LocalDate.now().toString()));
            verify(roomRepository,times(1)).findAllRoomsByDate(LocalDate.now());
            verify(sessionInputChecker, times(1)).checkDateFormat(any());
        }

        @Test
        void should_throwException400BadRequest_when_searchingAllSessionsFromAllRoomsByDate_and_dateFormatIncorrect(){
            when(sessionInputChecker.checkDateFormat(any())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));

            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> sessionService.findAllSessionsInAllRoomsByDate("20-2022-1"));
            assertEquals("Incorrect date. Correct format is: yyyy-mm-dd",exception.getMessage());
        }
        @Test
        void should_throwException404NotFound_when_searchingAllSessionsFromAllRoomsByDate_and_noRoomsExist(){
            when(sessionInputChecker.checkDateFormat(any())).thenReturn(LocalDate.now());
            when(roomRepository.findAllRoomsByDate(LocalDate.now())).thenReturn(null);

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.findAllSessionsInAllRoomsByDate("2022-02-01"));
            assertEquals("No rooms found on date:" + LocalDate.now(),exception.getMessage());
        }

        @Test
        void should_returnSessionsList_when_searchingSessionsInRoom_and_roomNotNull(){
            roomOne.addSession(session);
            when(roomRepository.findById(roomOne.getId())).thenReturn(Optional.ofNullable(roomOne));

            assertEquals(roomOne.getSessionList(), sessionService.getSingleRoomSessionsInADay(roomOne.getId()));
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_searchingSessionsInRoom_and_roomIsNull(){
            when(roomRepository.findById(roomOne.getId())).thenThrow(new NotFoundApiRequestException("Room with id:" + roomOne.getId() + " doesn't exist."));
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.getSingleRoomSessionsInADay(roomOne.getId()));
            assertEquals("Room with id:" + roomOne.getId() + " doesn't exist.",exception.getMessage());
        }

    }

    @Nested
    class SearchingSessionsinRoomWithParams{

        @Test
        void should_throwApiRequestException_when_searchingSessionsByParams_and_passedIncorrectData(){
            when(sessionInputChecker.checkDateFormat(dateString)).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> sessionService.findSessionsByParams(Optional.of(dateString),Optional.empty()));
            assertEquals("Incorrect date. Correct format is: yyyy-mm-dd",exception.getMessage());
        }

        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeAllDatePresent(){
            when(sessionInputChecker.checkDateFormat(dateString)).thenReturn(date);
            when(yogaSessionRepository.findYogaSessionByDateAndRoomIsNotNull(date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.of("all")));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeAllDateNotPresent(){
            when(yogaSessionRepository.findYogaSessionByRoomIsNotNull()).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.of("all")));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNoneDatePresent(){
            when(sessionInputChecker.checkDateFormat(dateString)).thenReturn(date);
            when(yogaSessionRepository.findYogaSessionByDateAndRoomIsNull(date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.of("none")));
        }

        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNoneDateNotPresent(){
            when(yogaSessionRepository.findYogaSessionByRoomIsNull()).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.of("none")));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypePresentDatePresent(){
            when(sessionInputChecker.checkDateFormat(dateString)).thenReturn(date);
            when(sessionInputChecker.checkRoomTypeFormat(roomTypeString)).thenReturn(yogaRoomType);
            when(yogaSessionRepository.findYogaSessionByRoomTypeAndDateAndRoomIsNotNull(yogaRoomType,date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.of(roomTypeString)));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypePresentDateNotPresent(){
            when(sessionInputChecker.checkRoomTypeFormat(roomTypeString)).thenReturn(yogaRoomType);
            when(yogaSessionRepository.findYogaSessionByRoomTypeAndRoomIsNotNull(yogaRoomType)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.of(roomTypeString)));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNotPresentDatePresent(){
            when(sessionInputChecker.checkDateFormat(dateString)).thenReturn(date);
            when( yogaSessionRepository.findYogaSessionByDateAndRoomIsNotNull(date)).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.of(dateString),Optional.empty()));
        }
        @Test
        void should_returnRoomUsingCorrectMethod_when_searchingSessionsByParams_and_roomTypeNotPresentDateNotPresent(){
            when(yogaSessionRepository.findAll()).thenReturn(sessionList);
            assertEquals(sessionList,sessionService.findSessionsByParams(Optional.empty(),Optional.empty()));
        }
    }

    @Nested
    class PatchingASession{

        @Test
        void should_updateSession_when_updatingSessionWithDateAndRoomType_and_roomOfSameTypeWithFreeSpaceAvailable() throws IOException, JsonPatchException {
            session.addMember(personOne);
            personOne.addSession(session);
            String myPatchInfo = updatePatchInfo.replace("2025-05-22",LocalDate.now().plusDays(1).toString());
            InputStream in = new ByteArrayInputStream(myPatchInfo.getBytes(StandardCharsets.UTF_8));
            jsonPatch = mapper.readValue(in, JsonPatch.class);

            JsonNode patched = jsonPatch.apply(mapper.convertValue(session, JsonNode.class));
            YogaSession patchedSession =  mapper.treeToValue(patched, YogaSession.class);

            when(sessionInputChecker.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(roomRepository.findRoomByDateAndRoomType(any(),any())).thenReturn(roomTwo);
            when(yogaSessionUtil.createAYogaSession(patchedSession.getDate(), patchedSession.getRoomType()
                    ,patchedSession.getStartOfSession(), patchedSession.getDuration())).thenReturn(patchedSession);
            when(roomServiceUtil.canAddSessionToRoom(roomTwo, patchedSession)).thenReturn(true);
            when(roomService.removeSessionFromRoom(session.getRoom().getId(), session.getId())).thenReturn(roomOne);
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));


            assertAll(
                    ()-> assertEquals(roomTwo.getRoomType(),(patchedSession.getRoomType())),
                    ()-> assertEquals(patchedSession, sessionService.patchSession("" + session.getId(), jsonPatch))
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
            when(roomRepository.findRoomByDateAndRoomType(any(),any())).thenReturn(roomTwo);
            when(yogaSessionUtil.createAYogaSession(patchedSession.getDate(), patchedSession.getRoomType()
                    ,patchedSession.getStartOfSession(), patchedSession.getDuration())).thenReturn(patchedSession);
            when(roomServiceUtil.canAddSessionToRoom(roomTwo, patchedSession)).thenReturn(false);
            when(roomService.removeSessionFromRoom(session.getRoom().getId(), session.getId())).thenReturn(roomOne);
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));


            assertAll(
                    ()-> assertEquals(roomTwo.getRoomType(),(patchedSession.getRoomType())),
                    ()-> assertNull(sessionService.patchSession("" + session.getId(), jsonPatch))
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

            assertEquals(patchedSession,sessionService.patchSession("" + session.getId(), jsonPatch));
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

            assertEquals(patchedSession,sessionService.patchSession("" + session.getId(), jsonPatch));
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

            Exception exception = assertThrows(ApiRequestException.class,()-> sessionService.patchSession("" + session.getId(), jsonPatch));

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

    @Nested
    class DeletingASession{
        @Test
        void should_deleteASession_when_deletingASession_and_sessionExists(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            sessionService.deleteASession(session.getId());
            verify(yogaSessionRepository,times(1)).delete(session);
        }
        @Test
        void should_throwException404NotFound_when_deletingASession_and_sessionDoesntExists(){
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.empty());
            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> sessionService.deleteASession(session.getId()));
            assertEquals("Yoga session id:" + session.getId() +  " not found.",exception.getMessage());
            verify(yogaSessionRepository,never()).delete(session);
        }
        @Test
        void should_removeReferencesToTheSession_when_deletingASession_and_sessionIsInARoom(){
            ArgumentCaptor<Room> roomArgumentCaptor = ArgumentCaptor.forClass(Room.class);
            roomOne.setId(23);
            session.setRoom(roomOne);
            roomOne.getSessionList().add(session);
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(roomRepository.save(roomArgumentCaptor.capture())).thenReturn(null);
             sessionService.deleteASession(session.getId());
             assertTrue(roomArgumentCaptor.getValue().getSessionList().isEmpty());
             assertEquals(roomArgumentCaptor.getValue().getId(),roomOne.getId());
             verify(roomRepository,times(1)).save(roomArgumentCaptor.getValue());
        }
        @Test
        void should_removeReferencesToTheSession_when_deletingASession_and_sessionHasMembers(){
            ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
            personOne.setId(321);
            session.addMember(personOne);
            personOne.addSession(session);
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.ofNullable(session));
            when(personRepository.save(personArgumentCaptor.capture())).thenReturn(null);
            sessionService.deleteASession(session.getId());
            assertTrue(personArgumentCaptor.getValue().getYogaSessions().isEmpty());
            assertEquals(personArgumentCaptor.getValue().getId(),personOne.getId());
        }

    }


}
