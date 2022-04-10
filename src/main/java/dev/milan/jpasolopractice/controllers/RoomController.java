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
            throw new ApiRequestException("Incorrect date. Correct format is: yyyy-mm-dd./400");
        }

        type = YogaRooms.valueOf(objectNode.get("type").asText().toUpperCase());
        openingHours = LocalTime.parse(objectNode.get("openingHours").asText());
        closingHours = LocalTime.parse(objectNode.get("closingHours").asText());

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
