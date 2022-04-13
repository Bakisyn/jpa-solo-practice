package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.RoomRepository;
import dev.milan.jpasolopractice.model.Room;

import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.websocket.server.PathParam;
import java.net.URI;
import java.util.List;

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

    @RequestMapping(value = "/rooms/{id}", method = RequestMethod.GET)
    public Room findRoomById(@PathVariable(value = "id") int roomId){
        return roomService.findRoomById(roomId);
    }

    @RequestMapping(value = "/rooms/dateandtype", method = RequestMethod.GET)
    public Room findRoomByRoomTypeAndDate(@RequestParam(value = "date") String date, @RequestParam(value = "type") String type){
        return roomService.findRoomByTypeAndDate(date, type);
    }
    @RequestMapping(value = "/rooms",method = RequestMethod.GET)
    public List<Room> findAllRooms(){
        return roomService.findAllRooms();
    }

    @RequestMapping(value = "/rooms/{id}/sessions", method = RequestMethod.GET)
    public List<YogaSession> getAllSessionsFromRoom(@PathVariable(value = "id")int roomId) throws NotFoundApiRequestException {
        return roomService.getSingleRoomSessionsInADay(roomId);
    }

    @RequestMapping(value = "/rooms/sessions", method = RequestMethod.GET)
    public List<YogaSession> getAllSessionFromAllRooms(@PathParam(value = "date") String date){
        return roomService.getAllRoomsSessionsInADay(date);
    }
    @RequestMapping(value = "/rooms/{roomId}/sessions/{sessionId}", method = RequestMethod.PUT)
    public ResponseEntity<?> addSessionToRoom(@PathVariable(value = "roomId") int roomId, @PathVariable("sessionId") int sessionId){
        YogaSession session = roomService.addSessionToRoom(roomId,sessionId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
        return ResponseEntity.created(location).body(session);
    }

    @RequestMapping(value = "/rooms/{roomId}/sessions/{sessionId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> removeSessionFromRoom(@PathVariable(value = "roomId") int roomId, @PathVariable(value = "sessionId") int sessionId){
        Room room = roomService.removeSessionFromRoom(roomId,sessionId);
        return ResponseEntity.ok(room);
    }
//    @RequestMapping(value = "/rooms/{id}",method = RequestMethod.DELETE)
//    public ResponseEntity<?> removeRoom(@PathVariable(value = "id") int roomId){
//        Room room = roomService.removeRoom(roomId);
//    }

}
