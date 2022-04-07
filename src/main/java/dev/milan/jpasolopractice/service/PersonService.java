package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
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
    public Person addPerson(String name, int age,String email) throws ApiRequestException {
        Person found = personRepository.findPersonByEmail(email);
        if (found == null){
            found = personServiceImpl.createPerson(name,age,email);
            if (found != null){
                personRepository.save(found);
                return found;
            }else{
                throw new ApiRequestException("Couldn't create person because of bad info.-400");
            }
        }else{
            throw new ApiRequestException("Person already exists.-409");
        }
//        return null;
    }
    public Person findPersonById(int id) throws ApiRequestException{
        Optional<Person> found = personRepository.findById(id);
        return found.orElseThrow(()-> new ApiRequestException("Person with that id couldn't be found.-404"));
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
