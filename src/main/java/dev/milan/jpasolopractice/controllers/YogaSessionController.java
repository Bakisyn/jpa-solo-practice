package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.YogaSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class YogaSessionController {
    @Autowired
    private YogaSessionService yogaSessionService;
    @RequestMapping(value="/sessions")
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
}
