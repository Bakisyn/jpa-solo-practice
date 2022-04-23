package dev.milan.jpasolopractice.yogasession;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
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
        String date = null, roomType = null, startTime = null, duration =  null;
        try{
             date = node.get("date").textValue();
             roomType = node.get("type").textValue();
             startTime = node.get("startTime").textValue();
             duration = node.get("duration").textValue();
        }catch (NullPointerException e){
            BadRequestApiRequestException.throwBadRequestException("Bad request data. Properties for session creation are: date, type, startTime, duration.");
        }
        YogaSession session = yogaSessionService.createAYogaSession(date,roomType,startTime,duration);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(session.getId()).toUri();
        return ResponseEntity.created(location).body(session);
    }



    @RequestMapping(value = "/sessions/{id}",method = RequestMethod.GET)
    public YogaSession findSessionById(@PathVariable(value = "id")  int sessionId){
        return yogaSessionService.findYogaSessionById(sessionId);
    }

    @RequestMapping(value = "/sessions", method = RequestMethod.GET)
    public List<YogaSession> findAllSessions(@PathParam("type")Optional<String> type, @PathParam("date") Optional<String> date){
        return yogaSessionService.findSessionsByParams(date, type);
    }


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

    @RequestMapping(value = "/rooms/{id}/sessions",method = RequestMethod.GET)
    public List<YogaSession> findAllSessionsInRoomByRoomId(@PathVariable(value = "id") int id){
        return yogaSessionService.findSingleRoomSessionsInADay(id);
    }

    @RequestMapping(value = "/sessions/{id}", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<YogaSession> updateYogaSession(@PathVariable("id") String id, @RequestBody JsonPatch patch) throws ApiRequestException{
        YogaSession result = yogaSessionService.patchSession(id, patch);
        if (result == null){
            return ResponseEntity.status(304).build();
        }else{
            return ResponseEntity.ok(result);
        }
    }

    @RequestMapping(value = "/sessions/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteASession(@PathVariable(value = "id") int id) throws NotFoundApiRequestException{
        yogaSessionService.deleteASession(id);
        return ResponseEntity.status(204).build();
    }


}
