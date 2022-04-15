package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.model.Room;

import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class RoomController {
    @Autowired
    private RoomService roomService;

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
    @RequestMapping(value = "/rooms", method = RequestMethod.GET)
    public List<Room> findAllRoomsBasedOnParams(@RequestParam(value = "date") Optional<String> date
            , @RequestParam(value = "type") Optional<String> type){
        return roomService.findAllRoomsBasedOnParams(date, type);
    }

    @RequestMapping(value = "/rooms/{id}", method = RequestMethod.GET)
    public Room findRoomById(@PathVariable(value = "id") int roomId){
        return roomService.findRoomById(roomId);
    }

    @RequestMapping(value = "/rooms/{id}",method = RequestMethod.DELETE)
    public ResponseEntity<?> removeRoom(@PathVariable(value = "id") int roomId) throws ApiRequestException{
        roomService.removeRoom(roomId);
        return ResponseEntity.noContent().build();
    }


    @RequestMapping(value = "/rooms/{roomId}/sessions/{sessionId}", method = RequestMethod.PUT)
    public ResponseEntity<?> addSessionToRoom(@PathVariable(value = "roomId") int roomId, @PathVariable("sessionId") int sessionId) throws ApiRequestException{
        YogaSession session = roomService.addSessionToRoom(roomId,sessionId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
        return ResponseEntity.created(location).body(session);
    }

    @RequestMapping(value = "/rooms/{roomId}/sessions/{sessionId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeSessionFromRoom(@PathVariable(value = "roomId") int roomId, @PathVariable(value = "sessionId") int sessionId) throws ApiRequestException{
        Room room = roomService.removeSessionFromRoom(roomId,sessionId);
        return ResponseEntity.ok(room);
    }



}
