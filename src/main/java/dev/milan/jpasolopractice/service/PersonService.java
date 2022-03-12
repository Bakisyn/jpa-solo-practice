package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.YogaSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonServiceImpl personServiceImpl;
    @Autowired
    public PersonService(PersonRepository personRepository, PersonServiceImpl personServiceImpl) {
        this.personRepository = personRepository;
        this.personServiceImpl = personServiceImpl;
    }

    @Transactional
    public Person addPerson(String name, int age,String email){
        Person found = personRepository.findPersonByEmail(email);
        if (found == null){
            found = personServiceImpl.createPerson(name,age,email);
            if (found != null){
                personRepository.save(found);
                return found;
            }
        }
        return null;
    }
    public Person findPersonById(int id){
        Optional<Person> found = personRepository.findById(id);
        return found.orElse(null);
    }
    public List<Person> findPeopleByName(String name){
        return personRepository.findPeopleByName(name);
    }
    @Transactional
    public boolean removeSession(Person person, YogaSession session) {
        if (person.getYogaSessions().contains(session)){
            person.getYogaSessions().remove(session);
            System.out.println("Removed session from person");
            return true;
        }
        return false;
    }
    @Transactional
    public List<YogaSession> getAllSessionsFromPerson(Person person){
        Person found = personRepository.findPersonByEmail(person.getEmail());
        if (found != null){
            return found.getYogaSessions();
        }
        return null;
    }
}
