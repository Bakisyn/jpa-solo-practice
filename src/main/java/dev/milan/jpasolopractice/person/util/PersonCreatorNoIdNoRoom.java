package dev.milan.jpasolopractice.person.util;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonCreatorNoIdNoRoom implements PersonCreator, CapitalizePersonName {
    @Autowired
    private final PersonFormatCheck personCheck;

    public PersonCreatorNoIdNoRoom(PersonFormatCheck personCheck) {
        this.personCheck = personCheck;
    }

    public Person createPerson(String name, int age, String email) throws ApiRequestException {
        if (personCheck.checkPersonData(name, age, email)){
            Person person = new Person();
            person.setName(capitalizeFirstLetters(name));
            person.setAge(age);
            person.setEmail(email);
            return person;
        }
        return null;
    }
}
