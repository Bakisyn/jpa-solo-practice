package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RoomService roomService;
    private String baseUrl;
    private Room room;
    private StringBuilder sb;

    @BeforeEach
    public void init(){
        baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        room = new Room();
        room.setId(1);
        room.setRoomType(YogaRooms.AIR_ROOM);
        room.setOpeningHours(LocalTime.of(8,30,0));
        room.setClosingHours(LocalTime.of(21,30,0));
        room.setDate(LocalDate.now().plusDays(10));

        sb = new StringBuilder("{\"date\":\"").append(room.getDate()).append("\",\"openingHours\":\"")
                .append(room.getOpeningHours()).append("\",\"closingHours\":\"").append(room.getClosingHours()).append("\",")
                .append("\"type\":\"").append(room.getRoomType().name()).append("\"}");
    }

    @Nested
    class CreatingARoom{
        @Test
        void should_callCreateMethodWithPassedParametersAndReturnCreatedStatusWithLocation() throws Exception {
            when(roomService.createARoom(room.getDate(),room.getOpeningHours(),room.getClosingHours(),room.getRoomType())).thenReturn(room);

            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON)
                            .content(sb.toString()))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.header().string("Location",baseUrl.concat("/rooms/"+ room.getId())))
                    .andExpect(content().string(asJsonString(room)));
        }

        @Test
        void should_throw409ApiRequestException_when_roomAlreadyExists() throws Exception {
            when(roomService.createARoom(room.getDate(),room.getOpeningHours(),room.getClosingHours(),room.getRoomType()))
                    .thenThrow(new ApiRequestException("Room id:" + room.getId() + " already exists./409"));

            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON)
                            .content(sb.toString()))
                    .andExpect(MockMvcResultMatchers.status().isConflict());
        }
    }



    @Test
    void should_returnCorrectRoom_when_searchingRoomById() throws Exception {
        when(roomService.findRoomById(1)).thenReturn(room);
        mockMvc.perform(get(baseUrl.concat("/rooms/1"))).andExpect(MockMvcResultMatchers.content().string(asJsonString(room)));
    }

    @Test
    void should_throwApiRequestException400NotFound_when_searchingRoomByIdAndRoomNotFound() throws Exception {
        when(roomService.findRoomById(1)).thenThrow(new ApiRequestException("Room with id:" + 1 + " doesn't exist./400"));
        mockMvc.perform(get(baseUrl.concat("/rooms/1"))).andExpect(MockMvcResultMatchers.status().isNotFound());
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
