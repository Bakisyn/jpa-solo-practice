package dev.milan.jpasolopractice.controllers;

import dev.milan.jpasolopractice.customException.ApiException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import javax.websocket.server.PathParam;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class PersonController {
    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    // localhost:8080/users   should return 201 Created status and location header  for newly created entity
    //409 Conflict response ako vec postoji  a ako ne moze da se napravi entity 400
    @RequestMapping(value = "/users",method = RequestMethod.POST)
    public ResponseEntity<?> addPerson(@RequestBody Person person) throws ApiRequestException {
        System.out.println("name " + person.getName() + " age " + person.getAge() + " email " + person.getEmail());
        Person created = personService.addPerson(person.getName(), person.getAge(),person.getEmail());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();

        return ResponseEntity.created(location).body(created);
    }
    // localhost:8080/users/{personId}
    @RequestMapping(value = "/users/{id}",method = RequestMethod.GET)
    public Person findPersonById(@PathVariable("id") int id) throws ApiRequestException{
        return personService.findPersonById(id);
    }

    // localhost:8080/users?name={userName}
//    public List<Person> findPeopleByName(String name){
//
//    }

    // localhost:8080/users/{personId}/sessions/{sessionId}
//    public boolean removeSession(Person person, YogaSession session) {
//
//    }

    // localhost:8080/users/{personId}/sessions/{sessionId}
//    public List<YogaSession> getAllSessionsFromPerson(Person person){
//
//    }


}
