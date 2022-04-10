package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.websocket.server.PathParam;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RoomController {
    @Autowired
    private RoomService roomService;

    @RequestMapping(value = "/rooms", method = RequestMethod.POST)
    public ResponseEntity<?> createARoom(@RequestBody ObjectNode objectNode) throws ApiRequestException {
        LocalDate date;
        YogaRooms type;
        LocalTime openingHours;
        LocalTime closingHours;
        try{
            date = LocalDate.parse(objectNode.get("date").textValue());
        }catch (Exception e){
            throw new ApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd/400");
        }
        try{
            type = YogaRooms.valueOf(objectNode.get("type").asText().toUpperCase());
        }catch (Exception e){
            throw new ApiRequestException("Incorrect type. Correct options are: AIR_ROOM, WATER_ROOM, EARTH_ROOM, FIRE_ROOM/400");
        }
        try{
            openingHours = LocalTime.parse(objectNode.get("openingHours").asText());
            closingHours = LocalTime.parse(objectNode.get("closingHours").asText());
        }catch (Exception e){
            throw new ApiRequestException("Incorrect openingHours or closingHours. Acceptable values range from: 00:00:00 to 23:59:59/400");
        }


        Room room = roomService.createARoom(date, openingHours, closingHours, type);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(room.getId()).toUri();
        return ResponseEntity.created(location).body(room);
    }

    @RequestMapping(value = "/rooms/{id}", method = RequestMethod.GET)
    public Room findRoomById(@PathVariable(value = "id") int roomId){
        return roomService.findRoomById(roomId);
    }

    @RequestMapping(value = "/rooms", method = RequestMethod.GET)
    public List<Room> findRoomsByRoomTypeAndDate(@RequestParam(value = "date") String date, @RequestParam(value = "type") String type){
        LocalDate datePassed = LocalDate.parse(date);
        YogaRooms roomTypePassed = YogaRooms.valueOf(type.toUpperCase());
        System.out.println("date: " + datePassed);
        System.out.println("YogaRooms " + roomTypePassed);
        return new ArrayList<>();
    }
}
