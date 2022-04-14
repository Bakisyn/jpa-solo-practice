package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonServiceImpl personServiceImpl;
    private final YogaSessionRepository yogaSessionRepository;
    @Autowired
    public PersonService(PersonRepository personRepository, PersonServiceImpl personServiceImpl, YogaSessionRepository yogaSessionRepository) {
        this.personRepository = personRepository;
        this.personServiceImpl = personServiceImpl;
        this.yogaSessionRepository = yogaSessionRepository;
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
    public Person findPersonById(int id) throws ApiRequestException{
        Optional<Person> found = personRepository.findById(id);
        return found.orElseThrow(()-> NotFoundApiRequestException.throwNotFoundException("Person id:" + id + " couldn't be found."));
    }
    public List<Person> findPeopleByName(String name) throws ApiRequestException{
        List<Person> foundPersons = personRepository.findPeopleByName(name);
        if (foundPersons.isEmpty()){
            NotFoundApiRequestException.throwNotFoundException("People named:" + name + " couldn't be found.");
        }
        return foundPersons;
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
}
