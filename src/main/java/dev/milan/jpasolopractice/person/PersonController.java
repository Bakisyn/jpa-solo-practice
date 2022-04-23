package dev.milan.jpasolopractice.person;

import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    @RequestMapping(value = "/users",method = RequestMethod.POST)
    public ResponseEntity<?> addPerson(@RequestBody Person person) throws ApiRequestException {
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
    public List<Person> findPeopleByParams(@RequestParam(value = "sessionId") Optional<String> sessionId, @RequestParam(value = "startAge")Optional<String> startAge
            , @RequestParam(value = "endAge")Optional<String> endAge) throws ApiRequestException{
        return personService.findPeopleByParams(sessionId, startAge, endAge);
    }


    @RequestMapping(value = "/users/{personId}/sessions",method = RequestMethod.GET)
    public List<YogaSession> getAllSessionsFromPerson(@PathVariable(value = "personId") int personId) throws ApiRequestException{
        return personService.getAllSessionsFromPerson(personId);
    }

    @RequestMapping(value = "/users/{id}", method = RequestMethod.PATCH, consumes = "application/json")
    public ResponseEntity<Person> updatePerson(@PathVariable("id") String id, @RequestBody JsonPatch patch) throws ApiRequestException{
            return ResponseEntity.ok(personService.patchPerson(id, patch));
    }
    @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deletePerson(@PathVariable(value = "id") int id) throws NotFoundApiRequestException {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

}
