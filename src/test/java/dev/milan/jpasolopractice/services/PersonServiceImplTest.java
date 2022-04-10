package dev.milan.jpasolopractice.services;

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
            assertNull(personServiceImplementation.createPerson(name,22,"alias@yahoo.com"));
        }
        @Test
        public void should_FailToCreatePerson_IfNameIsLongerThan100(){
            String name = "wdkaodoadoakdoaodsodoasdoaodoaodosoaowdoosoaowdoosoaowdkokaodkoakdoakdoakdoskoadkosakdsoadoaskdosakdokadokasodkasodkaodkaokdaowkoadoawodoaeaepldsalpdalwdoskaodkaokda";
            assertNull(personServiceImplementation.createPerson(name,20,"alias@yahoo.com"));
        }

        @Test
        public void should_FailToCreatePerson_IfAgeBelow10(){
            int age = 9;
            assertNull(personServiceImplementation.createPerson("Vaso Bakocevic",age,"alias@yahoo.com"));
        }
        @Test
        public void should_FailToCreatePerson_IfAgeAbove80(){
            int age = 81;
            assertNull(personServiceImplementation.createPerson("Vaso Bakocevic",age,"alias@yahoo.com"));
        }
        @Test
        public void should_FailToCreatePerson_IfEmailIsWrongFormat(){
            String email = "wjijdawd.c";
            Person person = personServiceImplementation.createPerson("Vaso Bakocevic",39,email);
            assertNull(person);
        }
    }

    @Test
    public void should_CapitalizeFirstLetters(){
        String name = "marko skoric";
        Person person = personServiceImplementation.createPerson(name,39,"alias@yahoo.com");
        assertEquals(person.getName(), "Marko Skoric");
    }


}
