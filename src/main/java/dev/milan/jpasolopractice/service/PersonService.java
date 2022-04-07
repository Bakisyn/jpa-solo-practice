package dev.milan.jpasolopractice.service;

import dev.milan.jpasolopractice.customException.ApiRequestException;
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
    public List<Person> findPeopleByName(String name) throws ApiRequestException{
        List<Person> foundPersons = personRepository.findPeopleByName(name);
        if (foundPersons.isEmpty()){
            throw new ApiRequestException("People with that name couldn't be found.-404");
        }
        return foundPersons;
    }
    @Transactional
    public boolean removeSession(int personId, int yogaSessionId) throws ApiRequestException{
        Person person = findPersonById(personId);
        Optional<YogaSession> found = yogaSessionRepository.findById(yogaSessionId);
        YogaSession session = found.orElseThrow(()-> new ApiRequestException("Yoga session with that id couldn't be found.-404"));
        if (person.getYogaSessions().contains(session)){
            person.getYogaSessions().remove(session);
            System.out.println("Removed session from person");
            return true;
        }
        return false;
    }
    @Transactional
    public List<YogaSession> getAllSessionsFromPerson(int personId) throws ApiRequestException{
        Optional<Person> found = personRepository.findById(personId);
        return found.map(Person::getYogaSessions).orElseThrow(()->new ApiRequestException("Person with that id couldn't be found.-404"));
    }
}
