package dev.milan.jpasolopractice.person.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.person.PersonRepository;
import dev.milan.jpasolopractice.shared.Patcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PersonPatcher implements Patcher<Person> {

    private ObjectMapper mapper;
    private PersonFormatCheck personFormatCheck;
    private PersonRepository personRepository;

    @Autowired
    public PersonPatcher(ObjectMapper mapper, PersonFormatCheck personFormatCheck, PersonRepository personRepository) {
        this.mapper = mapper;
        this.personFormatCheck = personFormatCheck;
        this.personRepository = personRepository;
    }

    @Override
    public Person patch(JsonPatch patch, Person person) throws ApiRequestException {
        Person personPatched = applyPatchToPerson(patch, person);
        if ((personFormatCheck.checkPersonData(personPatched.getName(),personPatched.getAge(),personPatched.getEmail()))){
            return updatePerson(person, personPatched);
        }else{
            return null;
        }

    }


    private Person updatePerson(Person oldPerson , Person personPatched) throws BadRequestApiRequestException {
        if (oldPerson.getId() != personPatched.getId()){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot change user id.");
        }else if(!Arrays.equals(oldPerson.getYogaSessions().toArray(), personPatched.getYogaSessions().toArray())){
            BadRequestApiRequestException.throwBadRequestException("Patch request cannot change user sessions.");
        }
        return personRepository.save(personPatched);
    }

    private Person applyPatchToPerson(JsonPatch patch, Person targetPerson) throws BadRequestApiRequestException {
        try {
            JsonNode patched = patch.apply(mapper.convertValue(targetPerson,JsonNode.class));
            return mapper.treeToValue(patched,Person.class);

        }catch (JsonPatchException | JsonProcessingException e){
            BadRequestApiRequestException.throwBadRequestException("Incorrect patch request data.");
        }
        return targetPerson;
    }
}
