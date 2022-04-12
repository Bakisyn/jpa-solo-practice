package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
            when(roomService.createARoom(room.getDate().toString(),room.getOpeningHours().toString(),room.getClosingHours().toString(),room.getRoomType().name())).thenReturn(room);

            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON)
                            .content(sb.toString()))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.header().string("Location",baseUrl.concat("/rooms/"+ room.getId())))
                    .andExpect(content().string(asJsonString(room)));
        }

        @Test
        void should_throwException409ConflictWithMessage_when_roomAlreadyExists() throws Exception {
            when(roomService.createARoom(room.getDate().toString(),room.getOpeningHours().toString(),room.getClosingHours().toString(),room.getRoomType().name()))
                    .thenThrow(new ConflictApiRequestException("Room id:" + room.getId() + " already exists."));

            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(jsonPath("$.message").value("Room id:" + room.getId() + " already exists."));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_passedIncorrectDateFormat() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            String badDate = sb.toString();
            badDate = badDate.replace(room.getDate().toString(),"04-2022-10");
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON)
                    .content(badDate))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect date. Correct format is: yyyy-mm-dd"));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_passedIncorrectOpeningTime() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
            String badTime = sb.toString();
            badTime = badTime.replace(room.getOpeningHours().toString(),"z1-20:10");
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(badTime))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_passedIncorrectClosingTime() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
            String badTime = sb.toString();
            badTime = badTime.replace(room.getClosingHours().toString(),"z1-20:10");
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(badTime))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_passedIncorrectRoomType() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect type. Correct options are: AIR_ROOM, WATER_ROOM, EARTH_ROOM, FIRE_ROOM"));
            String badType = sb.toString();
            badType = badType.replace(room.getRoomType().name(),"War_ROOM");
            System.out.println(badType);
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(badType))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect type. Correct options are: AIR_ROOM, WATER_ROOM, EARTH_ROOM, FIRE_ROOM"));
        }
    }

    @Nested
    class SearchingForARoom{
        @Test
        void should_returnCorrectRoom_when_searchingRoomById() throws Exception {
            when(roomService.findRoomById(1)).thenReturn(room);
            mockMvc.perform(get(baseUrl.concat("/rooms/1"))).andExpect(MockMvcResultMatchers.content().string(asJsonString(room)));
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_searchingRoomByIdAndRoomNotFound() throws Exception {
            when(roomService.findRoomById(1)).thenThrow(new NotFoundApiRequestException("Room with id:" + 1 + " doesn't exist."));

            mockMvc.perform(get(baseUrl.concat("/rooms/1"))).andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Room with id:" + 1 + " doesn't exist."));
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_searchingRoomByDateAndRoomTypeAndRoomIsNotFound() throws Exception {
            LocalDate date = LocalDate.now().plusDays(2);
            YogaRooms roomType = YogaRooms.values()[0];
            when(roomService.findRoomByTypeAndDate(date.toString(),roomType.name())).thenThrow(new NotFoundApiRequestException("Room on date:" + date + " ,of type:" + roomType.name() +" not found."));

            mockMvc.perform(get(baseUrl.concat("/rooms?date=" + date + "&type=" + roomType.name()))).andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Room on date:" + date + " ,of type:" + roomType.name() +" not found."));
        }
        @Test
        void should_returnRoom_when_searchingRoomByDateAndRoomTypeAndRoomIsFound() throws Exception {
            when(roomService.findRoomByTypeAndDate(room.getDate().toString(),room.getRoomType().name())).thenReturn(room);
            mockMvc.perform(get(baseUrl.concat("/rooms?date=" + room.getDate() + "&type=" + room.getRoomType().name()))).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(asJsonString(room)));
        }
        @Test
        void should_throwException409ConflictWithMessage_when_creatingRoomAndRoomAlreadyExists() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new ConflictApiRequestException("Room id:" + room.getId() + " already exists."));
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(sb.toString())).andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(jsonPath("$.message").value("Room id:" + room.getId() + " already exists."));
        }
        @Test
        void should_throwException400BadRequest_when_searchingRoomByDateAndRoomTypeWithBadFormat() throws Exception {
            when(roomService.findRoomByTypeAndDate(room.getDate().toString(),room.getRoomType().name())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            mockMvc.perform(get(baseUrl.concat("/rooms?date=" + room.getDate() + "&type=" + room.getRoomType().name()))).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect date. Correct format is: yyyy-mm-dd"));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_creatingRoomWithBadDate() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(sb.toString())).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
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
