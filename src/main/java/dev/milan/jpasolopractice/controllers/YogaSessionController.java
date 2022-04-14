package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.YogaSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.websocket.server.PathParam;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class YogaSessionController {
    @Autowired
    private YogaSessionService yogaSessionService;
    @RequestMapping(value="/sessions",method = RequestMethod.POST)
    public ResponseEntity<?> createAYogaSession(@RequestBody ObjectNode node){
        String date = node.get("date").textValue();
        String roomType = node.get("type").textValue();
        String startTime = node.get("startTime").textValue();
        String duration = node.get("duration").textValue();
        YogaSession session = yogaSessionService.createAYogaSession(date,roomType,startTime,duration);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(session.getId()).toUri();
        return ResponseEntity.created(location).body(session);
    }



    @RequestMapping(value = "/sessions/{id}",method = RequestMethod.GET)
    public YogaSession findSessionById(@PathVariable(value = "id")  int sessionId){
        return yogaSessionService.findYogaSessionById(sessionId);
    }
        //need to combine /sessions to search differently based on optional params ?type=byRoomType  or ?type=all  ?type=unassigned for orphans
//    @RequestMapping(value = "/sessions", method = RequestMethod.GET)
//    public List<YogaSession> findAllSessions(){
//        return yogaSessionService.findAllSessions();
//    }
//
//    @RequestMapping(value = "/rooms/sessions", method = RequestMethod.GET)
//    public List<YogaSession> findAllRoomsSessionsInADay(@PathParam(value = "date") String date){
//        return yogaSessionService.getAllRoomsSessionsInADay(date);
//    }
//    @RequestMapping(value = "/rooms/{id}/sessions", method = RequestMethod.GET)
//    public List<YogaSession> getAllSessionsFromRoom(@PathVariable(value = "id")int roomId) throws NotFoundApiRequestException {
//        return yogaSessionService.getSingleRoomSessionsInADay(roomId);
//    }

    @RequestMapping(value = "/sessions", method = RequestMethod.GET)
    public List<YogaSession> findAllSessions(@PathParam("type")Optional<String> type, @PathParam("date") Optional<String> date){
        return yogaSessionService.findSessionsByParams(date, type);
    }

    //////////////////////////////

    @RequestMapping(value = "/sessions/{sessionId}/users/{personId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removePersonFromSession(@PathVariable(value = "sessionId") int sessionId, @PathVariable(value = "personId") int personId) throws ApiRequestException {
        if (yogaSessionService.removeMemberFromYogaSession(sessionId,personId)){
            return ResponseEntity.noContent().build();
        }else{
            NotFoundApiRequestException.throwNotFoundException("Couldn't remove person id:" + personId + " from yoga session id: " + sessionId);
            return null;
        }
    }
    @RequestMapping(value = "/sessions/{sessionId}/users/{personId}", method = RequestMethod.PUT)
    public ResponseEntity<?> addPersonToSession(@PathVariable(value = "sessionId") int sessionId, @PathVariable(value = "personId") int personId) throws ApiRequestException {
        if (yogaSessionService.addMemberToYogaSession(sessionId,personId)){
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
            return ResponseEntity.created(location).build();
        }else{
            NotFoundApiRequestException.throwNotFoundException("Couldn't remove person id:" + personId + " from yoga session id: " + sessionId);
            return null;
        }
    }
}
