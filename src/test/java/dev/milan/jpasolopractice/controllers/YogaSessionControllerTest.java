package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
