package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.RoomType;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.YogaSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class YogaSessionControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private YogaSessionService yogaSessionService;
    private YogaSession session;
    private String dateString;
    private String roomTypeString;
    private String startTimeString;
    private String durationString;
    private LocalDate date;
    private RoomType roomType;
    private LocalTime startTime;
    private int duration;
    private String baseUrl;
    private Person person;
    private Room room;
    private LocalDate today;

    @BeforeEach
    void init(){
        date = LocalDate.now().plusDays(2);
        roomType = RoomType.values()[0];
        startTime = LocalTime.of(12,0,0);
        duration = 60;

        dateString = date.toString();
        roomTypeString = roomType.name();
        startTimeString = startTime.toString();
        durationString = "" + duration;

        session = new YogaSession();
        session.setDate(date);
        session.setRoomType(roomType);
        session.setStartOfSession(startTime);
        session.setDuration(duration);

        baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        person = new Person();
        person.setId(3);
        person.setName("Djosa");
        person.setEmail("djosa@hotmail.com");
        person.setAge(75);

        today = LocalDate.now();

        room = new Room();
        room.setId(1);
        room.setRoomType(RoomType.AIR_ROOM);
        room.setOpeningHours(LocalTime.of(8,30,0));
        room.setClosingHours(LocalTime.of(21,30,0));
        room.setDate(today.plusDays(10));
    }

    @Nested
    class CreatingASession {
        @Test
        void should_returnCreated200StatusWithLocation_when_creatingYogaSession_and_successfullyCreatedSession() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"date\":\"").append(dateString).append("\",\"type\":\"").append(roomTypeString).append("\",\"startTime\":\"").append(startTimeString)
                    .append("\",\"duration\":\"").append(durationString).append("\"}");
            when(yogaSessionService.createAYogaSession(dateString, roomTypeString, startTimeString, durationString)).thenReturn(session);
            mockMvc.perform(post(baseUrl.concat("/sessions")).contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                    .andExpect(status().isCreated()).andExpect(header().string("Location", baseUrl.concat("/sessions/" + session.getId())));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_creatingYogaSession_and_failedToCreateSession() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"date\":\"").append(dateString).append("\",\"type\":\"").append(roomTypeString).append("\",\"startTime\":\"").append(startTimeString)
                    .append("\",\"duration\":\"").append(durationString).append("\"}");
            when(yogaSessionService.createAYogaSession(anyString(), anyString(), anyString(), anyString())).thenThrow(new BadRequestApiRequestException("Date, room type, start time and duration must have values assigned."));
            mockMvc.perform(post(baseUrl.concat("/sessions")).contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Date, room type, start time and duration must have values assigned."));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_creatingYogaSession_and_usingWrongProperties() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"date\":\"").append(dateString).append("\",\"roomType\":\"").append(roomTypeString).append("\",\"startTime\":\"").append(startTimeString)
                    .append("\",\"duration\":\"").append(durationString).append("\"}");
            mockMvc.perform(post(baseUrl.concat("/sessions")).contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Bad request data. Properties for session creation are: date, type, startTime, duration."));
        }

    }

    @Nested
    class SearchingForSession{

        @Test
        void should_returnSession_when_searchingSessionsById_and_sessionExist() throws Exception {
            when(yogaSessionService.findYogaSessionById(session.getId())).thenReturn(session);
            mockMvc.perform(get(baseUrl.concat("/sessions/" + session.getId()))).andExpect(content().string(asJsonString(session)));
        }

        @Test
        void should_throwException404NotFound_when_searchingSessionsById_and_sessionDoesntExist() throws Exception {
            when(yogaSessionService.findYogaSessionById(session.getId())).thenThrow(new NotFoundApiRequestException("Yoga session with that id couldn't be found."));
            mockMvc.perform(get(baseUrl.concat("/sessions/" + session.getId()))).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Yoga session with that id couldn't be found."));
        }

        @Test
        void should_returnSessionList_when_searchingSessionsListForSingleRoom_and_roomExist() throws Exception {
            room.addSession(session);
            when(yogaSessionService.getSingleRoomSessionsInADay(anyInt())).thenReturn(room.getSessionList());
            mockMvc.perform(get(baseUrl.concat("/rooms/" + room.getId() + "/sessions"))).andExpect(status().isOk())
                    .andExpect(content().string(asJsonString(room.getSessionList())));
        }
        @Test
        void should_throwException_when_searchingSessionsListForSingleRoom_and_roomDoesntExist() throws Exception {
            when(yogaSessionService.getSingleRoomSessionsInADay(room.getId())).thenThrow(new NotFoundApiRequestException("Room id:" + room.getId() + " not found"));
            mockMvc.perform(get(baseUrl.concat("/rooms/" + room.getId() + "/sessions"))).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Room id:" + room.getId() + " not found"));
        }

        @Test
        void should_returnListOfRooms_when_searchingSessionsByParams_and_passedCorrectInfo() throws Exception {
            when(yogaSessionService.findSessionsByParams(Optional.empty(),Optional.empty())).thenReturn(List.of(session));
            mockMvc.perform(get(baseUrl.concat("/sessions"))).andExpect(status().isOk())
                    .andExpect(content().string(asJsonString(List.of(session))));
        }
        @Test
        void should_throwException400BadFormat_when_searchingSessionsByParams_and_badFormat() throws Exception {
            when(yogaSessionService.findSessionsByParams(Optional.of("21ds-223-11"),Optional.empty())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            mockMvc.perform(get(baseUrl.concat("/sessions?date=21ds-223-11"))).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect date. Correct format is: yyyy-mm-dd"));
        }

    }

    @Nested
    class RemovingPersonFromSession{
        @Test
        void should_return204NoContentStatus_when_removingPersonFromSession_and_personRemovedSuccessfully() throws Exception {
            when(yogaSessionService.removeMemberFromYogaSession(session.getId(),person.getId())).thenReturn(true);

            mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());
        }
        @Test
        void should_throwException404NotFound_when_removingPersonFromSession_and_sessionNotFound() throws Exception {
            when(yogaSessionService.removeMemberFromYogaSession(session.getId(),person.getId())).thenThrow(new NotFoundApiRequestException("Yoga session id:" + session.getId() +  " couldn't be found."));

            mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(jsonPath("$.message").value("Yoga session id:" + session.getId() +  " couldn't be found."));
        }

    }
    @Nested
    class AddingPersonToSession{
        @Test
        void should_return200Created_when_addingPersonToSession_and_successfullyAddedUserToSession() throws Exception {
            when(yogaSessionService.addMemberToYogaSession(session.getId(),person.getId())).thenReturn(true);

            mockMvc.perform(put(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(header().string("Location",baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())));
        }
        @Test
        void should_throwException404NotFound_when_addingPersonToSession_and_userAlreadyPresent() throws Exception {
            when(yogaSessionService.removeMemberFromYogaSession(session.getId(),person.getId())).thenThrow(new NotFoundApiRequestException("User id:" + person.getId() + " already present in session id:" + session.getId()));

            mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(jsonPath("$.message").value("User id:" + person.getId() + " already present in session id:" + session.getId()));
        }
    }

    @Nested
    class PatchingAYogaSession{
        @Test
        void should_returnOkStatusWithResultingSession_when_updatingYogaSession_and_successfullyUpdated() throws Exception {
            String updatePatchInfo = "[\n" +
                    "    {\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/startOfSession\", \"value\":\"13:00:00\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/duration\", \"value\":\"60\"}\n" +
                    "]";
            ArgumentCaptor<JsonPatch> captured = ArgumentCaptor.forClass(JsonPatch.class);
            InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
            JsonPatch jsonPatch = mapper.readValue(in, JsonPatch.class);
            when(yogaSessionService.patchSession(eq("" + session.getId()), captured.capture())).thenReturn(session);
            mockMvc.perform(patch(baseUrl.concat("/sessions/" + session.getId())).contentType(MediaType.APPLICATION_JSON)
                    .content(updatePatchInfo)).andExpect(status().isOk()).andExpect(content().string(asJsonString(session)));
            assertEquals(mapper.writeValueAsString(jsonPatch),mapper.writeValueAsString(captured.getValue()));
        }

        @Test
        void should_return304Status_when_updatingYogaSession_and_cantUpdateSession() throws Exception {
            String updatePatchInfo = "[\n" +
                    "    {\"op\":\"replace\",\"path\":\"/roomType\", \"value\":\"EARTH_ROOM\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2025-05-22\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/startOfSession\", \"value\":\"13:00:00\"},\n" +
                    "    {\"op\":\"replace\",\"path\":\"/duration\", \"value\":\"60\"}\n" +
                    "]";
            when(yogaSessionService.patchSession(eq("" + session.getId()), any())).thenReturn(null);
            mockMvc.perform(patch(baseUrl.concat("/sessions/" + session.getId())).contentType(MediaType.APPLICATION_JSON)
                    .content(updatePatchInfo)).andExpect(status().is(304));
        }
    }

    @Test
    void should_return204Status_when_deletingASession_and_successfullyDeleted() throws Exception {
        doNothing().when(yogaSessionService).deleteASession(session.getId());
        mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId()))).andExpect(status().isNoContent());
    }
    @Test
    void should_throwException404NotFound_when_deletingASession_and_sessionNotFound() throws Exception {
        doThrow(new NotFoundApiRequestException("Yoga session id:" + session.getId() +  " not found.")).when(yogaSessionService).deleteASession(session.getId());
        mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId()))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Yoga session id:" + session.getId() +  " not found."));
    }



    public static String asJsonString(final Object obj){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");
            objectMapper.setDateFormat(df);
            return objectMapper.registerModule(new JavaTimeModule()).writeValueAsString(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
