package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.RoomType;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.RoomService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RoomService roomService;
    @Autowired
    ObjectMapper mapper;
    private String baseUrl;
    private Room room;
    private StringBuilder sb;
    private YogaSession session;
    private final LocalDate today = LocalDate.now();
    private String roomSessionUrl;

    @BeforeEach
    public void init(){
        baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        room = new Room();
        room.setId(1);
        room.setRoomType(RoomType.AIR_ROOM);
        room.setOpeningHours(LocalTime.of(8,30,0));
        room.setClosingHours(LocalTime.of(21,30,0));
        room.setDate(today.plusDays(10));


        session = new YogaSession();
        session.setId(3);
        session.setDate(today.plusDays(2));
        session.setStartOfSession(LocalTime.of(8,0,0));
        session.setEndOfSession(LocalTime.of(10,0,0));

        roomSessionUrl = baseUrl.concat("/rooms/" + room.getId() + "/sessions/" + session.getId());

        sb = new StringBuilder("{\"date\":\"").append(room.getDate()).append("\",\"openingHours\":\"")
                .append(room.getOpeningHours()).append("\",\"closingHours\":\"").append(room.getClosingHours()).append("\",")
                .append("\"type\":\"").append(room.getRoomType().name()).append("\"}");
    }

    @Nested
    class CreatingARoom{
        @Test
        void should_throwException400BadRequestWithMessage_when_creatingRoom_and_IncorrectDate() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(sb.toString())).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
        }
        @Test
        void should_callCreateMethodWithPassedParameters_and_returnCreatedStatusWithLocation__when_creatingRoom_and_creationSuccessful() throws Exception {
            when(roomService.createARoom(room.getDate().toString(),room.getOpeningHours().toString(),room.getClosingHours().toString(),room.getRoomType().name())).thenReturn(room);

            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON)
                            .content(sb.toString()))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.header().string("Location",baseUrl.concat("/rooms/"+ room.getId())))
                    .andExpect(content().string(asJsonString(room)));
        }

        @Test
        void should_throwException409ConflictWithMessage_when_creatingRoom_and_roomAlreadyExists() throws Exception {
            when(roomService.createARoom(room.getDate().toString(),room.getOpeningHours().toString(),room.getClosingHours().toString(),room.getRoomType().name()))
                    .thenThrow(new ConflictApiRequestException("Room id:" + room.getId() + " already exists."));

            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(sb.toString()))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(jsonPath("$.message").value("Room id:" + room.getId() + " already exists."));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_creatingRoom_and_passedIncorrectDateFormat() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            String badDate = sb.toString();
            badDate = badDate.replace(room.getDate().toString(),"04-2022-10");
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON)
                    .content(badDate))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect date. Correct format is: yyyy-mm-dd"));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_creatingRoom_and_passedIncorrectOpeningTime() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
            String badTime = sb.toString();
            badTime = badTime.replace(room.getOpeningHours().toString(),"z1-20:10");
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(badTime))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_creatingRoom_and_passedIncorrectClosingTime() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
            String badTime = sb.toString();
            badTime = badTime.replace(room.getClosingHours().toString(),"z1-20:10");
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(badTime))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59"));
        }
        @Test
        void should_throwException400BadRequestWithMessage_when_creatingRoom_and_passedInvalidParameters() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new BadRequestApiRequestException("Incorrect type. Correct options are: AIR_ROOM, WATER_ROOM, EARTH_ROOM, FIRE_ROOM"));
            String badType = sb.toString();
            badType = badType.replace("openingHours","blabla");
            System.out.println(badType);
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(badType))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Bad request data. Properties for room creation are: date, type, openingHours, closingHours."));
        }
    @Test
    void should_throwException400BadRequestWithMessage_when_creatingRoom_and_passedIncorrectRoomType() throws Exception {
    String badType = sb.toString();
    badType = badType.replace(room.getRoomType().name(),"War_ROOM");
    System.out.println(badType);
    when(roomService.createARoom(anyString(), anyString(), anyString(), anyString())).thenThrow(new BadRequestApiRequestException("Incorrect type. Correct options are: AIR_ROOM, WATER_ROOM, EARTH_ROOM, FIRE_ROOM"));
    mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(badType))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Incorrect type. Correct options are: AIR_ROOM, WATER_ROOM, EARTH_ROOM, FIRE_ROOM"));
        }
    }

    @Nested
    class SearchingForARoom{
        @Test
        void should_returnCorrectRoom_when_searchingRoomById_and_roomFound() throws Exception {
            when(roomService.findRoomById(1)).thenReturn(room);
            mockMvc.perform(get(baseUrl.concat("/rooms/1"))).andExpect(MockMvcResultMatchers.content().string(asJsonString(room)));
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_searchingRoomById_and_roomNotFound() throws Exception {
            when(roomService.findRoomById(1)).thenThrow(new NotFoundApiRequestException("Room with id:" + 1 + " doesn't exist."));

            mockMvc.perform(get(baseUrl.concat("/rooms/1"))).andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Room with id:" + 1 + " doesn't exist."));
        }

        @Test
        void should_returnRooms_when_searchingForRoomsWithParams_and_datePresentRoomTypePresent_and_roomIsFound() throws Exception {
            List<Room> roomList = new ArrayList<>();
            roomList.add(room);
            when(roomService.findAllRoomsBasedOnParams(Optional.of(room.getDate().toString()),Optional.of(room.getRoomType().name()))).thenReturn(roomList);
            mockMvc.perform(get(baseUrl.concat("/rooms?date=" + room.getDate() + "&type=" + room.getRoomType().name()))).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(asJsonString(roomList)));
        }
        @Test
        void should_throwException409ConflictWithMessage_when_creatingRoom_and_roomAlreadyExists() throws Exception {
            when(roomService.createARoom(anyString(),anyString(),anyString(),anyString())).thenThrow(new ConflictApiRequestException("Room id:" + room.getId() + " already exists."));
            mockMvc.perform(post(baseUrl.concat("/rooms")).contentType(MediaType.APPLICATION_JSON).content(sb.toString())).andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(jsonPath("$.message").value("Room id:" + room.getId() + " already exists."));
        }
        @Test
        void should_throwException400BadRequest_when_searchingForRoomsWithParams_and_datePresentRoomTypePresent_and_incorrectDataFormat() throws Exception {
            when(roomService.findAllRoomsBasedOnParams(any(),any())).thenThrow(new BadRequestApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd"));
            mockMvc.perform(get(baseUrl.concat("/rooms?date=" + room.getDate() + "&type=" + room.getRoomType().name()))).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Incorrect date. Correct format is: yyyy-mm-dd"));
        }


        @Test
        void should_returnListOfRooms_when_searchingForRoomsWithParams_and_noParamsPresent() throws Exception {
            List<Room> roomList = new ArrayList<>();
            roomList.add(room);
            when(roomService.findAllRoomsBasedOnParams(Optional.empty(),Optional.empty())).thenReturn(roomList);
            mockMvc.perform(get(baseUrl.concat("/rooms"))).andExpect(status().isOk())
                    .andExpect(content().string(asJsonString(roomList)));
        }
        @Test
        void should_returnListOfRooms_when_searchingForRoomsWithParams_and_typeParamPresent() throws Exception {
            List<Room> roomList = new ArrayList<>();
            roomList.add(room);
            when(roomService.findAllRoomsBasedOnParams(Optional.empty(),Optional.of(RoomType.values()[0].name()))).thenReturn(roomList);
            mockMvc.perform(get(baseUrl.concat("/rooms?type=" + RoomType.values()[0].name()))).andExpect(status().isOk())
                    .andExpect(content().string(asJsonString(roomList)));
        }
        @Test
        void should_returnListOfRooms_when_searchingForRoomsWithParams_and_dateParamPresent() throws Exception {
            List<Room> roomList = new ArrayList<>();
            roomList.add(room);
            when(roomService.findAllRoomsBasedOnParams(Optional.of(room.getDate().toString()),Optional.empty())).thenReturn(roomList);
            mockMvc.perform(get(baseUrl.concat("/rooms?date=" + room.getDate()))).andExpect(status().isOk())
                    .andExpect(content().string(asJsonString(roomList)));
        }

    }

    @Nested
    class AddingSessionToRoom{

        @Test
        void should_returnCreatedStatusWithLocation_when_addingSessionToARoom_and_addingSuccessful() throws Exception {
            when(roomService.addSessionToRoom(room.getId(),session.getId())).thenReturn(session);
            mockMvc.perform(put(roomSessionUrl))
                    .andExpect(status().isCreated()).andExpect(content().string(asJsonString(session)))
                    .andExpect(header().string("Location",roomSessionUrl));
        }
        @Test
        void should_throwException404WithMessage_when_addingSessionToARoom_and_addingNotSuccessful() throws Exception {
            when(roomService.addSessionToRoom(room.getId(),session.getId())).thenThrow(new NotFoundApiRequestException("Room id:" + room.getId() + " not found."));
            mockMvc.perform(put(roomSessionUrl)).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Room id:" + room.getId() + " not found."));
        }


    }

    @Nested
    class RemovingSessionFromRoom{

        @Test
        void should_returnOkStatusWithRoom_when_removingSessionFromARoom_and_removedSuccessfully() throws Exception {
            room.addSession(session);
            when(roomService.removeSessionFromRoom(room.getId(),session.getId())).thenReturn(room);
            mockMvc.perform(delete(roomSessionUrl)).andExpect(status().isOk()).andExpect(content().string(asJsonString(room)));
        }
        @Test
        void should_throwException404NotFound_when_removingSessionFromARoom_and_serviceMethodThrows() throws Exception {
            when(roomService.removeSessionFromRoom(room.getId(),session.getId())).thenThrow(new NotFoundApiRequestException("Yoga session with id:" + session.getId() + " doesn't exist."));
            mockMvc.perform(delete(roomSessionUrl)).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Yoga session with id:" + session.getId() + " doesn't exist."));
        }
    }

    @Nested
    class PatchingARoom{

@Test
void should_returnOkStatusWithResultingRoom_when_updatingRoom_and_successfullyUpdated() throws Exception {
    String updatePatchInfo = "[{\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2022-05-23\"}]";
    ArgumentCaptor<JsonPatch> captured = ArgumentCaptor.forClass(JsonPatch.class);
    InputStream in = new ByteArrayInputStream(updatePatchInfo.getBytes(StandardCharsets.UTF_8));
    JsonPatch jsonPatch = mapper.readValue(in, JsonPatch.class);
    when(roomService.patchRoom(eq("" + room.getId()), captured.capture())).thenReturn(room);
    mockMvc.perform(patch(baseUrl.concat("/rooms/" + room.getId())).contentType(MediaType.APPLICATION_JSON)
            .content(updatePatchInfo)).andExpect(status().isOk()).andExpect(content().string(asJsonString(room)));
    assertEquals(mapper.writeValueAsString(jsonPatch),mapper.writeValueAsString(captured.getValue()));
}

        @Test
        void should_return304Status_when_updatingRoom_and_cantUpdateRoom() throws Exception {
            String updatePatchInfo = "[{\"op\":\"replace\",\"path\":\"/date\", \"value\":\"2022-05-23\"}]";
            when(roomService.patchRoom(eq("" + room.getId()), any())).thenReturn(null);
            mockMvc.perform(patch(baseUrl.concat("/rooms/" + room.getId())).contentType(MediaType.APPLICATION_JSON)
                    .content(updatePatchInfo)).andExpect(status().is(304));
        }
    }

    @Nested
    class DeletingARoom{
        @Test
        void should_returnNoContentStatus_when_removingARoom_and_roomSuccessfullyRemoved() throws Exception {
            mockMvc.perform(delete(baseUrl.concat("/rooms/" + room.getId()))).andExpect(status().isNoContent());
            verify(roomService,times(1)).removeRoom(room.getId());
        }
        @Test
        void should_throwException404NotFound_when_removingARoom_and_roomNotFound() throws Exception {
            doThrow(new NotFoundApiRequestException("Room with id:" + room.getId() + " doesn't exist.")).when(roomService).removeRoom(room.getId());
            mockMvc.perform(delete(baseUrl.concat("/rooms/" + room.getId()))).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Room with id:" + room.getId() + " doesn't exist."));
            verify(roomService,times(1)).removeRoom(room.getId());
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
