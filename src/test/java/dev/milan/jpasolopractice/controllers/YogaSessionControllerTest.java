package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class YogaSessionControllerTest {
    @Autowired
    MockMvc mockMvc;
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
    class CreatingASession{
        @Test
        void should_returnCreated200StatusWithLocation_when_successfullyCreatedSession() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n" +
                    "    \"date\":\"" + dateString + "\",\n" +
                    "    \"type\":\"" + roomTypeString + "\",\n" +
                    "    \"startTime\":\"" + startTimeString + "\",\n" +
                    "    \"duration\":\"" + durationString + "\"\n" +
                    "}");
            when(yogaSessionService.createAYogaSession(dateString,roomTypeString,startTimeString,durationString)).thenReturn(session);
            mockMvc.perform(post(baseUrl.concat("/sessions")).contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                    .andExpect(status().isCreated()).andExpect(header().string("Location",baseUrl.concat("/sessions/" + session.getId())));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_failedToCreateSession() throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n" +
                    "    \"date\":\"" + dateString + "\",\n" +
                    "    \"type\":\"" + roomTypeString + "\",\n" +
                    "    \"startTime\":\"" + startTimeString + "\",\n" +
                    "    \"duration\":\"" + durationString + "\"\n" +
                    "}");
            when(yogaSessionService.createAYogaSession(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Date, room type, start time and duration must have values assigned."));
            mockMvc.perform(post(baseUrl.concat("/sessions")).contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Date, room type, start time and duration must have values assigned."));
        }
    }

    @Nested
    class SearchingForSession{
        @Test
        void should_returnAllSessions_when_searchingForAllSessions() throws Exception {
            List<YogaSession> yogaSessions = new ArrayList<>();
            yogaSessions.add(session);
            when(yogaSessionService.findAllSessions()).thenReturn(yogaSessions);
            mockMvc.perform(get(baseUrl.concat("/sessions"))).andExpect(content().string(asJsonString(yogaSessions)));
        }

        @Test
        void should_returnSession_when_searchingByIdAndSessionExist() throws Exception {
            when(yogaSessionService.findYogaSessionById(session.getId())).thenReturn(session);
            mockMvc.perform(get(baseUrl.concat("/sessions/" + session.getId()))).andExpect(content().string(asJsonString(session)));
        }

        @Test
        void should_throwException404NotFound_when_searchingByIdAndSessionNotExist() throws Exception {
            when(yogaSessionService.findYogaSessionById(session.getId())).thenThrow(new NotFoundApiRequestException("Yoga session with that id couldn't be found."));
            mockMvc.perform(get(baseUrl.concat("/sessions/" + session.getId()))).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Yoga session with that id couldn't be found."));
        }

        @Test
        void should_returnSessionList_when_searchingSessionsListForAllRoomsAndRoomsExist() throws Exception {
            room.addSession(session);
            when(yogaSessionService.getAllRoomsSessionsInADay(anyString())).thenReturn(room.getSessionList());
            mockMvc.perform(get(baseUrl.concat("/rooms/sessions?date=" + today))).andExpect(status().isOk())
                    .andExpect(content().string(asJsonString(room.getSessionList())));
        }
        @Test
        void should_throwException_when_searchingSessionsListForAllRoomsAndRoomsNotExist() throws Exception {
            when(yogaSessionService.getAllRoomsSessionsInADay(today.toString())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            mockMvc.perform(get(baseUrl.concat("/rooms/sessions/?date=" + today))).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect date. Correct format is: yyyy-mm-dd"));
        }

        @Test
        void should_returnSessionList_when_searchingSessionsListForSingleRoomAndRoomExist() throws Exception {
            room.addSession(session);
            when(yogaSessionService.getSingleRoomSessionsInADay(anyInt())).thenReturn(room.getSessionList());
            mockMvc.perform(get(baseUrl.concat("/rooms/" + room.getId() + "/sessions"))).andExpect(status().isOk())
                    .andExpect(content().string(asJsonString(room.getSessionList())));
        }
        @Test
        void should_throwException_when_searchingSessionsListForSingleRoomAndRoomNotExist() throws Exception {
            when(yogaSessionService.getSingleRoomSessionsInADay(room.getId())).thenThrow(new NotFoundApiRequestException("Room id:" + room.getId() + " not found"));
            mockMvc.perform(get(baseUrl.concat("/rooms/" + room.getId() + "/sessions"))).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Room id:" + room.getId() + " not found"));
        }

        @Test
        void should_returnListOfRooms_when_searchingSessionsByParams() throws Exception {
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
        void should_return204NoContentStatus_when_successfullyRemovedUserFromSession() throws Exception {
            when(yogaSessionService.removeMemberFromYogaSession(session.getId(),person.getId())).thenReturn(true);

            mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());
        }
        @Test
        void should_throwException404NotFound_when_sessionNotFound() throws Exception {
            when(yogaSessionService.removeMemberFromYogaSession(session.getId(),person.getId())).thenThrow(new NotFoundApiRequestException("Yoga session id:" + session.getId() +  " couldn't be found."));

            mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(jsonPath("$.message").value("Yoga session id:" + session.getId() +  " couldn't be found."));
        }

    }
    @Nested
    class AddingPersonToSession{
        @Test
        void should_return200Created_when_successfullyAddedUserToSession() throws Exception {
            when(yogaSessionService.addMemberToYogaSession(session.getId(),person.getId())).thenReturn(true);

            mockMvc.perform(put(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(header().string("Location",baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())));
        }
        @Test
        void should_throwException404NotFound_when_userAlreadyInSession() throws Exception {
            when(yogaSessionService.removeMemberFromYogaSession(session.getId(),person.getId())).thenThrow(new NotFoundApiRequestException("User id:" + person.getId() + " already present in session id:" + session.getId()));

            mockMvc.perform(delete(baseUrl.concat("/sessions/" + session.getId() +  "/users/" + person.getId())))
                    .andExpect(MockMvcResultMatchers.status().isNotFound()).andExpect(jsonPath("$.message").value("User id:" + person.getId() + " already present in session id:" + session.getId()));
        }

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
