package dev.milan.jpasolopractice.person.util;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
@SpringBootTest
public class CreatePersonNoIdNoRoomTest {
    @Autowired
    private CreatePersonNoIdNoRoom createPersonNoIdNoRoom;
    @MockBean
    private PersonEmailNameAgeCheck personEmailNameAgeCheck;
    private String name;
    private int age;
    private String email;
    private Person person;

    @BeforeEach
    void init(){
        name = "Vs Ramachandran";
        age = 39;
        email = "alias@yahoo.com";
        person  = new Person();
        person.setName(name);
        person.setAge(age);
        person.setEmail(email);
    }

    @Test
    void should_returnPersonWithSameData_when_creatingAPerson_and_passedDataIsCorrect(){
        String lowName = name.toLowerCase();
        when(personEmailNameAgeCheck.checkPersonData(lowName,age,email)).thenReturn(true);
        assertEquals(person, createPersonNoIdNoRoom.createPerson(lowName,age,email));
    }
    @Test
    void should_throwException_when_creatingAPerson_and_passedDataIsIncorrect(){
        when(personEmailNameAgeCheck.checkPersonData(name,age,email)).thenThrow(new BadRequestApiRequestException("testMessage"));
        Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> createPersonNoIdNoRoom.createPerson(name,age,email));
        assertEquals("testMessage",exception.getMessage());
    }

    @Test
    public void should_capitalizeFirstLetters_when_creatingAPerson(){
        String lowName = name.toLowerCase();
        when(personEmailNameAgeCheck.checkPersonData(lowName,age,email)).thenReturn(true);
        Person person = createPersonNoIdNoRoom.createPerson(lowName,age,email);
        assertEquals(person.getName(), name);
    }
}
