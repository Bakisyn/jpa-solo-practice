package dev.milan.jpasolopractice;

import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.service.PersonServiceImpl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PersonServiceImplTest {
    static PersonServiceImpl service;
    static Person person1;
    static Person person2;
    static Person person3;

    @BeforeAll
    public static void initialize(){
        service = new PersonServiceImpl();
        person1 = new Person();
        person2 = new Person();
        person3 = new Person();
    }
    @Nested
    class CreatePersonTest {
        @Test
        public void should_FailToCreatePerson_IfNameIsNotAllCharacters(){
            String name = "sok&od";
            assertNull(service.createPerson(name,22,"alias@yahoo.com"));
        }
        @Test
        public void should_FailToCreatePerson_IfNameIsLongerThan100(){
            String name = "wdkaodoadoakdoaodsodoasdoaodoaodosoaowdoosoaowdoosoaowdkokaodkoakdoakdoakdoskoadkosakdsoadoaskdosakdokadokasodkasodkaodkaokdaowkoadoawodoaeaepldsalpdalwdoskaodkaokda";
            assertNull(service.createPerson(name,20,"alias@yahoo.com"));
        }

        @Test
        public void should_FailToCreatePerson_IfAgeBelow10(){
            int age = 9;
            assertNull(service.createPerson("Vaso Bakocevic",age,"alias@yahoo.com"));
        }
        @Test
        public void should_FailToCreatePerson_IfAgeAbove80(){
            int age = 81;
            assertNull(service.createPerson("Vaso Bakocevic",age,"alias@yahoo.com"));
        }
        @Test
        public void should_FailToCreatePerson_IfEmailIsWrongFormat(){
            String email = "wjijdawd.c";
            Person person = service.createPerson("Vaso Bakocevic",39,email);
            assertNull(person);
        }
    }

    @Test
    public void should_CapitalizeFirstLetters(){
        String name = "marko skoric";
        Person person = service.createPerson(name,39,"alias@yahoo.com");
        assertEquals(person.getName(), "Marko Skoric");
    }
}
