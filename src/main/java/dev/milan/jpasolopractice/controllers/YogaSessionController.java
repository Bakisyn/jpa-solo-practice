package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.YogaSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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

    @RequestMapping(value = "/sessions", method = RequestMethod.GET)
    public List<YogaSession> findAllSessions(){
        return yogaSessionService.findAllSessions();
    }
    @RequestMapping(value = "/sessions/{id}",method = RequestMethod.GET)
    public YogaSession findSessionById(@PathVariable(value = "id")  int sessionId){
        return yogaSessionService.findYogaSessionById(sessionId);
    }
}
