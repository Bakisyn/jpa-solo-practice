package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.data.RoomRepository;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class RoomController {
    @Autowired
    private RoomService roomService;
    @Autowired
    private RoomRepository roomRepository;

    @RequestMapping(value = "/rooms", method = RequestMethod.POST)
    public ResponseEntity<?> createARoom(@RequestBody ObjectNode objectNode) throws ApiRequestException {
        String date =  objectNode.get("date").textValue();
        String type = objectNode.get("type").asText().toUpperCase();
        String openingHours = objectNode.get("openingHours").asText();
        String closingHours = objectNode.get("closingHours").asText();

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
    public Room findRoomByRoomTypeAndDate(@RequestParam(value = "date") String date, @RequestParam(value = "type") String type){
        return roomService.findRoomByTypeAndDate(date, type);
    }
}
