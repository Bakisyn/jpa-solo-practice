package dev.milan.jpasolopractice.services;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonService;
import dev.milan.jpasolopractice.service.PersonServiceImpl;
import dev.milan.jpasolopractice.service.YogaSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    @MockBean
    private YogaSessionService yogaSessionService;
    @MockBean
    private YogaSessionRepository yogaSessionRepository;

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
        void should_addPersonToRepo_when_personNotFound(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(null);
            when(personServiceImpl.createPerson(NAME,AGE,EMAIL)).thenReturn(personOne);

            assertEquals(personOne, personService.addPerson(NAME,AGE,EMAIL));
            verify(personServiceImpl,times(1)).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,times(1)).save(personOne);
        }
        @Test
        void should_notAddPersonToRepo_when_personAlreadyPresent(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(personOne);

            Exception exception = assertThrows(ConflictApiRequestException.class,()->personService.addPerson(NAME,AGE,EMAIL));

            assertEquals("Person already exists.",exception.getMessage());
            verify(personServiceImpl,never()).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,never()).save(any());
        }

        @Test
        void should_notAddPersonToRepo_when_personInfoIncorrect(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(null);
            when(personServiceImpl.createPerson(NAME,AGE,EMAIL)).thenThrow(new BadRequestApiRequestException("Template Message."));

            Exception exception = assertThrows(ApiRequestException.class, ()-> personService.addPerson(NAME,AGE,EMAIL));

            assertEquals("Template Message.",exception.getMessage());
            verify(personServiceImpl,times(1)).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,never()).save(any());
        }
    }

    @Nested
    class SearchForPeople{
        @Test
        void should_returnPerson_when_personFoundInTheRepoById(){
            Optional<Person> person = Optional.of(personOne);
            when(personRepository.findById(anyInt())).thenReturn(person);
            assertEquals(personOne, personService.findPersonById(12));
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_personNotFoundById(){
            int id = 12;
            when(personRepository.findById(anyInt())).thenReturn(Optional.empty());

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> personService.findPersonById(id));

            assertEquals("Person id:" + id + " couldn't be found.",exception.getMessage());
        }

        @Test
        void should_returnPersonList_when_peopleFoundInRepoByName(){
            List<Person> persons = new ArrayList<>();
            persons.add(personOne);
            when(personRepository.findPeopleByName(anyString())).thenReturn(persons);
            assertEquals(1, personService.findPeopleByName("Stefanija").size());
        }

        @Test
        void should_throwException404NotFoundWithMessage_when_peopleNotFoundInRepoByName(){
            List<Person> persons = new ArrayList<>();
            String name = "bubi";
            when(personRepository.findPeopleByName(name)).thenReturn(persons);

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> personService.findPeopleByName(name));
            assertEquals(exception.getMessage(), "People named:" + name + " couldn't be found.");
        }
    }
    @Nested
    class RemovingSessionFromPerson{
        @Test
        void should_returnTrue_when_yogaSessionRemovedFromPerson(){
            when(personRepository.findById(anyInt())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            personOne.addSession(session);
            assertTrue(personService.removeSessionFromPerson(personOne.getId(),session.getId()));
        }
        @Test
        void should_returnFalse_when_personDoesntContainYogaSession(){
            when(personRepository.findById(anyInt())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));

            assertFalse(personService.removeSessionFromPerson(personOne.getId(),session.getId()));
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_personNotFound(){
            int id = 12;
            when(personRepository.findById(id)).thenReturn(Optional.empty());
            when(yogaSessionRepository.findById(anyInt())).thenReturn(Optional.of(session));
            Exception exception = assertThrows(ApiRequestException.class, ()->personService.removeSessionFromPerson(12,29));
            assertEquals("Person id:" + id + " couldn't be found.",exception.getMessage());
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_yogaSessionNotFound(){
            int id = 12;
            when(personRepository.findById(anyInt())).thenReturn(Optional.of(personOne));
            when(yogaSessionRepository.findById(anyInt())).thenThrow(new NotFoundApiRequestException("Yoga session id:" + id + " couldn't be found."));

            Exception exception = assertThrows(ApiRequestException.class, ()->personService.removeSessionFromPerson(12,29));
            assertEquals("Yoga session id:" + id + " couldn't be found.",exception.getMessage());
        }
    }


    @Test
    void should_returnAllSessionsFromPerson_when_personIsFoundInRepo(){
        personOne.addSession(session);
        when(personRepository.findById(any())).thenReturn(Optional.of(personOne));
        assertEquals(1,personService.getAllSessionsFromPerson(personOne.getId()).size());
    }
    @Test
    void should_returnNull_when_personNotFoundInRepo(){
        when(personRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ApiRequestException.class, ()-> personService.getAllSessionsFromPerson(personOne.getId()));
        assertEquals("Person id:" + personOne.getId() + " couldn't be found.",exception.getMessage());
    }
}
