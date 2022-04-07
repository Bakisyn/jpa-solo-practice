package dev.milan.jpasolopractice.controllers;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
public class PersonController {
    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @RequestMapping(value = "/users",method = RequestMethod.POST)
    public ResponseEntity<?> addPerson(@RequestBody Person person) throws ApiRequestException {
        System.out.println("name " + person.getName() + " age " + person.getAge() + " email " + person.getEmail());
        Person created = personService.addPerson(person.getName(), person.getAge(),person.getEmail());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();

        return ResponseEntity.created(location).body(created);
    }
    @RequestMapping(value = "/users/{id}",method = RequestMethod.GET)
    public Person findPersonById(@PathVariable("id") int id) throws ApiRequestException{
        return personService.findPersonById(id);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<Person> findPeopleByName(@RequestParam(value = "name") String name) throws ApiRequestException{
        return personService.findPeopleByName(name);
    }

    @RequestMapping(value = "/users/{personId}/sessions/{sessionId}",method = RequestMethod.PATCH)
    public ResponseEntity<?> removeSession(@PathVariable(value = "personId") int personId, @PathVariable(value = "sessionId") int sessionId) throws ApiRequestException{
        if (personService.removeSession(personId,sessionId)){
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/users/{id}")
                    .buildAndExpand(personId).toUri();

            return ResponseEntity.ok().header("Location",location.toString()).body(findPersonById(personId));
        }else{
            return ResponseEntity.badRequest().body("Yoga session was not found in user's yoga sessions.");
        }
    }

    @RequestMapping(value = "/users/{personId}/sessions",method = RequestMethod.GET)
    public List<YogaSession> getAllSessionsFromPerson(@PathVariable(value = "personId") int personId) throws ApiRequestException{
        return personService.getAllSessionsFromPerson(personId);
    }


}
