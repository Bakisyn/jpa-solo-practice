package dev.milan.jpasolopractice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonServiceImpl personServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;
    private final FormatCheckService formatCheckService;
    private final ObjectMapper objectMapper;
    @Autowired
    public PersonService(PersonRepository personRepository, PersonServiceImpl personServiceImpl, YogaSessionRepository yogaSessionRepository,
                         FormatCheckService formatCheckService, ObjectMapper objectMapper) {
        this.personRepository = personRepository;
        this.personServiceImpl = personServiceImpl;
        this.yogaSessionRepository = yogaSessionRepository;
        this.formatCheckService = formatCheckService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Person addPerson(String name, int age,String email) throws ApiRequestException {
        Person found = personRepository.findPersonByEmail(email);
        if (found == null){
            found = personServiceImpl.createPerson(name,age,email);
            personRepository.save(found);
            return found;
        }else{
            ConflictApiRequestException.throwConflictApiRequestException("Person already exists.");
        }
        return null;
    }
    public Person findPersonById(int id) throws NotFoundApiRequestException{
        Optional<Person> found = personRepository.findById(id);
        return found.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Person id:" + id + " couldn't be found."));
    }
    public boolean removeSessionFromPerson(Person person, YogaSession session) throws ApiRequestException{
        if (person.getYogaSessions().contains(session)) {
            person.getYogaSessions().remove(session);
            return true;
        }
        NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + session.getId() + " not found in user id:" + person.getId() + " sessions.");
        return false;
    }
    @Transactional
    public List<YogaSession> getAllSessionsFromPerson(int personId) throws ApiRequestException{
        Optional<Person> found = personRepository.findById(personId);
        return found.map(Person::getYogaSessions).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Person id:" + personId + " couldn't be found."));
    }

    public boolean addSessionToPerson(YogaSession session, Person person) throws ApiRequestException{
        if (!person.getYogaSessions().contains(session)) {
            person.addSession(session);
            return true;
        }
        ConflictApiRequestException.throwConflictApiRequestException("Yoga session id:" + session.getId() + " already present in user id:" + person.getId() + " sessions.");
        return false;
    }

    public List<Person> findPeopleByParams(Optional<String> sessionId, Optional<String> startAge, Optional<String> endAge) throws ApiRequestException{
        if (startAge.isEmpty() || endAge.isEmpty()){
            if (sessionId.isEmpty()){
                return (List<Person>) personRepository.findAll();
            }else{
                YogaSession session = findSessionById(formatCheckService.checkNumberFormat(sessionId.get()));
                return session.getMembersAttending();
            }
        }else{
            int startAgeNum = formatCheckService.checkNumberFormat(startAge.get());
            int endAgeNum = formatCheckService.checkNumberFormat(endAge.get());
            if (startAgeNum > endAgeNum){
                BadRequestApiRequestException.throwBadRequestException("startAge cannot be larger than endAge");
            }
            if (sessionId.isEmpty()){
                return findPeopleByAge(startAgeNum, endAgeNum);
            }else{
                YogaSession session = findSessionById(formatCheckService.checkNumberFormat(sessionId.get()));
                return session.getMembersAttending().stream().filter(s -> s.getAge() >= startAgeNum && s.getAge() <= endAgeNum).collect(Collectors.toList());
            }
        }
    }

    private YogaSession findSessionById(Integer sessionId) throws NotFoundApiRequestException {
        return yogaSessionRepository.findById(sessionId).orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Yoga session id:" + sessionId + " not found."));

    }

    private List<Person> findPeopleByAge(Integer startAge, Integer endAge) {
        return personRepository.findPeopleByAgeBetween(startAge,endAge);
    }


    public Person patchPerson(String id, JsonPatch patch) throws NotFoundApiRequestException, BadRequestApiRequestException {
        Person person = findPersonById(formatCheckService.checkNumberFormat(id));
        Person personPatched = applyPatchToPerson(patch, person);
        if ((personServiceImpl.createPerson(personPatched.getName(),personPatched.getAge(),personPatched.getEmail()) != null)){
            return updatePerson(person, personPatched);
        }else{
            return null;
        }

    }


    private Person updatePerson(Person oldPerson , Person personPatched) throws BadRequestApiRequestException{
        if (oldPerson.getId() != personPatched.getId()){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot change user id.");
        }else if(!Arrays.equals(oldPerson.getYogaSessions().toArray(), personPatched.getYogaSessions().toArray())){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot change user sessions.");
        }
        return personRepository.save(personPatched);
    }

    private Person applyPatchToPerson(JsonPatch patch, Person targetPerson) throws BadRequestApiRequestException {
        try {
            JsonNode patched = patch.apply(objectMapper.convertValue(targetPerson,JsonNode.class));
            return objectMapper.treeToValue(patched,Person.class);

        }catch (JsonPatchException | JsonProcessingException e){
            BadRequestApiRequestException.throwBadRequestException("Incorrect patch request data.");
        }
        return targetPerson;
    }
    @Transactional
    public void deletePerson(int id) throws NotFoundApiRequestException{
        Person personToDelete = findPersonById(id);
        for (YogaSession session: personToDelete.getYogaSessions()){
            session.removeMember(personToDelete);
            yogaSessionRepository.save(session);
        }
        personRepository.delete(personToDelete);
    }
}
