package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonServiceImpl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

public class PersonServiceImplTest {
    static PersonServiceImpl personServiceImplementation;
    static Person person1;
    static Person person2;
    static Person person3;

    @BeforeAll
    public static void initialize(){
        personServiceImplementation = new PersonServiceImpl();
        person1 = new Person();
        person2 = new Person();
        person3 = new Person();
    }
    @Nested
    class CreatePersonTest {
        @Test
        public void should_FailToCreatePerson_IfNameIsNotAllCharacters(){
            String name = "sok&od";
            Exception exception = assertThrows(ApiRequestException.class, () -> personServiceImplementation.createPerson(name,22,"alias@yahoo.com"));
            assertEquals("Bad name formatting. Name must only contain alphabetical characters and be below 100 characters in length./400",exception.getMessage());
        }
        @Test
        public void should_FailToCreatePerson_IfNameIsLongerThan100(){
            String name = "wdkaodoadoakdoaodsodoasdoaodoaodosoaowdoosoaowdoosoaowdkokaodkoakdoakdoakdoskoadkosakdsoadoaskdosakdokadokasodkasodkaodkaokdaowkoadoawodoaeaepldsalpdalwdoskaodkaokda";
            Exception exception = assertThrows(ApiRequestException.class, () -> personServiceImplementation.createPerson(name,20,"alias@yahoo.com"));
            assertEquals("Bad name formatting. Name must only contain alphabetical characters and be below 100 characters in length./400",exception.getMessage());
        }

        @Test
        public void should_FailToCreatePerson_IfAgeBelowMinimumAge(){
            int minAge = personServiceImplementation.getMIN_AGE();
            int maxAge = personServiceImplementation.getMAX_AGE();
            Exception exception = assertThrows(ApiRequestException.class, () -> personServiceImplementation.createPerson("Vaso Bakocevic",minAge-1,"alias@yahoo.com"));
            assertEquals("Age must be between " + minAge + " and " + maxAge + "./400" , exception.getMessage());
        }
        @Test
        public void should_FailToCreatePerson_IfAgeAbove80(){
            int minAge = personServiceImplementation.getMIN_AGE();
            int maxAge = personServiceImplementation.getMAX_AGE();
            Exception exception = assertThrows(ApiRequestException.class, () -> personServiceImplementation.createPerson("Vaso Bakocevic",maxAge+1,"alias@yahoo.com"));
            assertEquals("Age must be between " + minAge + " and " + maxAge + "./400" , exception.getMessage());
        }
        @Test
        public void should_FailToCreatePerson_IfEmailIsWrongFormat(){
            String email = "wjijdawd.c";
            Exception exception = assertThrows(ApiRequestException.class, () -> personServiceImplementation.createPerson("Vaso Bakocevic",39,email));
            assertEquals("Incorrect email format. Email must only contain alphabetical characters, numbers, and one @ and end with .com or .org or .net./400", exception.getMessage());
        }
    }

    @Test
    public void should_CapitalizeFirstLetters(){
        String name = "marko skoric";
        Person person = personServiceImplementation.createPerson(name,39,"alias@yahoo.com");
        assertEquals(person.getName(), "Marko Skoric");
    }


}
