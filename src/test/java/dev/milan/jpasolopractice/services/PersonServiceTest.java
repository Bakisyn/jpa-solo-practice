package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaRooms;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonService;
import dev.milan.jpasolopractice.service.PersonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PersonServiceTest {
    @Autowired
    private PersonService personService;
    @MockBean
    private PersonRepository personRepository;
    @MockBean
    private PersonServiceImpl personServiceImpl;

    private Person personOne;
    private YogaSession session;
    private final String NAME = "Marija";
    private final int AGE = 24;
    private final String EMAIL = "fifticent@yahoo.com";

    @BeforeEach
    void init(){
        personOne = new Person();
        personOne.setEmail("example@hotmail.com");
        personOne.setAge(33);
        personOne.setName("Badji");
        personOne.setName("Kukumber");

        session = new YogaSession();
        session.setStartOfSession(LocalTime.of(9,0,0));
        session.setDuration(45);
        session.setDate(LocalDate.now().plus(20, ChronoUnit.DAYS));
        session.setRoom(new Room());



    }
    @Nested
    class AddAPerson{
        @Test
        void should_AddPersonToRepo_When_PersonIsNotFound(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(null);
            when(personServiceImpl.createPerson(NAME,AGE,EMAIL)).thenReturn(personOne);

            assertEquals(personOne, personService.addPerson(NAME,AGE,EMAIL));
            verify(personServiceImpl,times(1)).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,times(1)).save(personOne);
        }
        @Test
        void should_NotAddPersonToRepo_When_PersonAlreadyPresent(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(personOne);

            Exception exception = assertThrows(ApiRequestException.class,()->personService.addPerson(NAME,AGE,EMAIL));

            assertEquals("Person already exists.-409",exception.getMessage());
            verify(personServiceImpl,never()).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,never()).save(any());
        }

        @Test
        void should_NotAddPersonToRepo_When_PersonPersonInfoIncorrect(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(null);
            when(personServiceImpl.createPerson(NAME,AGE,EMAIL)).thenReturn(null);

            Exception exception = assertThrows(ApiRequestException.class, ()-> personService.addPerson(NAME,AGE,EMAIL));

            assertEquals("Couldn't create person because of bad info.-400",exception.getMessage());
            verify(personServiceImpl,times(1)).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,never()).save(any());
        }
    }

    @Nested
    class SearchForPeople{
        @Test
        void should_ReturnPerson_When_PersonIsFoundInTheRepoById(){
            Optional<Person> person = Optional.of(personOne);
            when(personRepository.findById(anyInt())).thenReturn(person);
            assertEquals(personOne, personService.findPersonById(12));
        }
        @Test
        void should_ThrowException_When_PersonIsNotFound(){
            when(personRepository.findById(anyInt())).thenReturn(Optional.empty());

            Exception exception = assertThrows(ApiRequestException.class, ()-> personService.findPersonById(12));

            assertEquals("Person with that id couldn't be found.-404",exception.getMessage());
        }

        @Test
        void should_ReturnPersonList_When_PersonIsFoundInTheRepoByName(){
            List<Person> persons = new ArrayList<>();
            persons.add(personOne);
            when(personRepository.findPeopleByName(anyString())).thenReturn(persons);
            assertEquals(1, personService.findPeopleByName("Stefanija").size());
        }

        @Test
        void should_ReturnEmptyList_When_PersonIsNotFoundInTheRepoByName(){
            List<Person> persons = new ArrayList<>();
            when(personRepository.findPeopleByName(anyString())).thenReturn(persons);
            assertEquals(0, personService.findPeopleByName("Stefanija").size());
        }
    }


    @Test
    void should_ReturnTrue_When_YogaSessionIsRemovedFromPerson(){
        personOne.addSession(session);
        assertTrue(personService.removeSession(personOne,session));
    }
    @Test
    void should_ReturnFalse_When_YogaSessionCantBeRemovedFromPerson(){
        assertFalse(personService.removeSession(personOne,session));
    }


    @Test
    void should_ReturnAllSessionsFromPerson_When_PersonIsFoundInRepo(){
        personOne.addSession(session);
        when(personRepository.findPersonByEmail(personOne.getEmail())).thenReturn(personOne);
        assertEquals(1,personService.getAllSessionsFromPerson(personOne).size());
    }
    @Test
    void should_ReturnNull_When_PersonIsNotFoundInRepo(){
        when(personRepository.findPersonByEmail(personOne.getEmail())).thenReturn(null);
        assertNull(personService.getAllSessionsFromPerson(personOne));
    }
}
