package dev.milan.jpasolopractice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
import dev.milan.jpasolopractice.data.PersonRepository;
import dev.milan.jpasolopractice.data.YogaSessionRepository;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.FormatCheckService;
import dev.milan.jpasolopractice.service.PersonService;
import dev.milan.jpasolopractice.service.PersonServiceImpl;
import dev.milan.jpasolopractice.service.YogaSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
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
    @MockBean
    private FormatCheckService formatCheckService;
    @Autowired
    ObjectMapper mapper;

    private Person personOne;
    private YogaSession session;
    private final String NAME = "Marija";
    private final int AGE = 24;
    private final String EMAIL = "fifticent@yahoo.com";
    private List<Person> personList;
    private Person copyOfPersonOne;
    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @BeforeEach
    void init(){
        personOne = new Person();
        personOne.setEmail("example@hotmail.com");
        personOne.setAge(33);
        personOne.setName("Badji");
        personOne.setName("Kukumber");
        personOne.setId(4);

        session = new YogaSession();
        session.setStartOfSession(LocalTime.of(9,0,0));
        session.setDuration(45);
        session.setDate(LocalDate.now().plus(20, ChronoUnit.DAYS));
        session.setRoom(new Room());

        personList = new ArrayList<>();
        personList.add(personOne);
        personOne.addSession(session);
        copyOfPersonOne = (Person) personOne.clone();
        personCaptor = ArgumentCaptor.forClass(Person.class);


    }
    @Nested
    class AddAPerson{
        @Test
        void should_addPersonToRepo_when_addingPersonToRepo_and_personNotFound(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(null);
            when(personServiceImpl.createPerson(NAME,AGE,EMAIL)).thenReturn(personOne);

            assertEquals(personOne, personService.addPerson(NAME,AGE,EMAIL));
            verify(personServiceImpl,times(1)).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,times(1)).save(personOne);
        }
        @Test
        void should_notAddPersonToRepo_when_addingPersonToRepo_and_personAlreadyPresent(){
            when(personRepository.findPersonByEmail(EMAIL)).thenReturn(personOne);

            Exception exception = assertThrows(ConflictApiRequestException.class,()->personService.addPerson(NAME,AGE,EMAIL));

            assertEquals("Person already exists.",exception.getMessage());
            verify(personServiceImpl,never()).createPerson(anyString(),anyInt(),anyString());
            verify(personRepository,never()).save(any());
        }

        @Test
        void should_notAddPersonToRepo_when_addingPersonToRepo_and_personInfoIncorrect(){
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
        void should_returnPerson_when_searchingPersonById_and_personFoundInTheRepoById(){
            Optional<Person> person = Optional.of(personOne);
            when(personRepository.findById(anyInt())).thenReturn(person);
            assertEquals(personOne, personService.findPersonById(12));
        }
        @Test
        void should_throwException404NotFoundWithMessage_when_searchingPersonById_and_personNotFoundById(){
            int id = 12;
            when(personRepository.findById(anyInt())).thenReturn(Optional.empty());

            Exception exception = assertThrows(NotFoundApiRequestException.class, ()-> personService.findPersonById(id));

            assertEquals("Person id:" + id + " couldn't be found.",exception.getMessage());
        }

        @Test
        void should_returnListOfAllPeople_when_searchingPeopleWithParams_and_noParamsPassed(){
            when(personRepository.findAll()).thenReturn(personList);
            assertEquals(personList, personService.findPeopleByParams(Optional.empty(),Optional.empty(),Optional.empty()));
        }
        @Test
        void should_returnListOfPeopleByAge_when_searchingPeopleWithParams_and_startAgeEndAgePassed(){
            when(formatCheckService.checkNumberFormat("42")).thenReturn(42);
            when(formatCheckService.checkNumberFormat("43")).thenReturn(43);
            when(personRepository.findPeopleByAgeBetween(anyInt(),anyInt())).thenReturn(personList);
            assertEquals(personList, personService.findPeopleByParams(Optional.empty(),Optional.of("42"),Optional.of("43")));
        }
        @Test
        void should_returnListOfPeopleBySessionId_when_searchingPeopleWithParams_and_sessionIdPassed(){
            session.addMember(personOne);
            session.setId(3);
            when(formatCheckService.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            assertEquals(session.getMembersAttending(), personService.findPeopleByParams(Optional.of("" + session.getId()),Optional.empty(),Optional.empty()));
        }
        @Test
        void should_returnListOfPeopleBySessionIdAndAge_when_searchingPeopleWithParams_and_sessionIdStartAgeEndAgePassed(){
            Person tooYoung = new Person();
            tooYoung.setAge(12);
            Person tooOld = new Person();
            tooOld.setAge(99);
            Person searchedAge = new Person();
            searchedAge.setAge(42);
            session.setId(3);
            session.addMember(tooYoung);
            session.addMember(tooOld);
            session.addMember(searchedAge);

            when(formatCheckService.checkNumberFormat("42")).thenReturn(42);
            when(formatCheckService.checkNumberFormat("43")).thenReturn(43);
            when(formatCheckService.checkNumberFormat("" + session.getId())).thenReturn(session.getId());
            when(yogaSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

            assertEquals(List.of(searchedAge), personService.findPeopleByParams(Optional.of("" + session.getId()), Optional.of("42"),Optional.of("43")));
        }
        @Test
        void should_throwException404NotFound_when_searchingPeopleWithParams_and_sessionIdIncorrectFormatPassed(){
            when(formatCheckService.checkNumberFormat("" + session.getId())).thenThrow(new BadRequestApiRequestException("Number must be an integer value."));
            Exception exception = assertThrows(BadRequestApiRequestException.class,()-> personService.findPeopleByParams(Optional.of("" + session.getId()), Optional.empty(), Optional.empty()));
            assertEquals("Number must be an integer value.", exception.getMessage());
        }
        @Test
        void should_throwException400BadRequest_when_searchingPeopleWithParams_and_startAgeOrEndAgeIncorrectFormatPassed(){
            when(formatCheckService.checkNumberFormat(anyString())).thenThrow(new BadRequestApiRequestException(""));
            assertThrows(BadRequestApiRequestException.class,()-> personService.findPeopleByParams(Optional.empty(),Optional.of("23"),Optional.of("32")));
        }
        @Test
        void should_throwException400BadRequest_when_searchingPeopleWithParams_and_startAgeLargerThanEndAge(){
            when(formatCheckService.checkNumberFormat("5")).thenReturn(5);
            when(formatCheckService.checkNumberFormat("4")).thenReturn(4);
            Exception exception = assertThrows(BadRequestApiRequestException.class,()-> personService.findPeopleByParams(Optional.empty(),Optional.of("5"),Optional.of("4")));
            assertEquals("startAge cannot be larger than endAge",exception.getMessage());
        }


    }
    @Nested
    class RemovingSessionFromPerson{
        @Test
        void should_returnTrue_when_removingSessionFromPerson_and_yogaSessionRemovedFromPerson(){
            personOne.addSession(session);
            assertTrue(personService.removeSessionFromPerson(personOne,session));
        }
        @Test
        void should_throwException404NotFound_when_removingSessionFromPerson_and_personDoesntContainYogaSession(){
            personOne.setYogaSessions(new ArrayList<>());
            Exception exception = assertThrows(NotFoundApiRequestException.class,()-> personService.removeSessionFromPerson(personOne,session));
            assertEquals("Yoga session id:" + session.getId() + " not found in user id:" + personOne.getId() + " sessions.", exception.getMessage());

        }

    }
    @Nested
    class AddingSessionToPerson{

        @Test
        void should_returnTrue_when_addingSessionToPerson_and_sessionNotPresent(){
            personOne.setYogaSessions(new ArrayList<>());
            assertTrue(personService.addSessionToPerson(session,personOne));
        }
        @Test
        void should_throwException409Conflict_when_addingSessionToPerson_and_SessionAlreadyPresent(){
            personOne.addSession(session);
            Exception exception = assertThrows(ConflictApiRequestException.class, ()-> personService.addSessionToPerson(session,personOne));
            assertEquals("Yoga session id:" + session.getId() + " already present in user id:" + personOne.getId() + " sessions.", exception.getMessage());
        }
    }


    @Test
    void should_returnAllSessionsFromPerson_when_searchingAllSessionsFromPerson_and_personIsFoundInRepo(){
        when(personRepository.findById(any())).thenReturn(Optional.of(personOne));
        assertEquals(1,personService.getAllSessionsFromPerson(personOne.getId()).size());
    }
    @Test
    void should_returnNull_when_searchingAllSessionsFromPerson_and_personNotFoundInRepo(){
        when(personRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ApiRequestException.class, ()-> personService.getAllSessionsFromPerson(personOne.getId()));
        assertEquals("Person id:" + personOne.getId() + " couldn't be found.",exception.getMessage());
    }

    @Nested
    class PatchingPerson{
        @Test
        void should_returnUpdatedPerson_when_patchingPerson_and_passedCorrectDate() throws IOException {
            copyOfPersonOne.setEmail("zzomn@hotmail.com");
            String patchInfo = "[{ \"op\": \"replace\", \"path\": \"/email\", \"value\": \"zzomn@hotmail.com\" }]";
            InputStream in = new ByteArrayInputStream(patchInfo.getBytes(StandardCharsets.UTF_8));
            JsonPatch patch = mapper.readValue(in, JsonPatch.class);
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            when(personRepository.save(copyOfPersonOne)).thenReturn(copyOfPersonOne);

            assertEquals(copyOfPersonOne,personService.patchPerson("" + personOne.getId(),patch));
        }
        @Test
        void should_throwException400BadRequest_when_patchingPerson_and_patchChangingUserId() throws IOException {
            copyOfPersonOne.setEmail("zzomn@hotmail.com");
            String patchInfo = "[{ \"op\": \"replace\", \"path\": \"/id\", \"value\": \"5545\" }]";
            InputStream in = new ByteArrayInputStream(patchInfo.getBytes(StandardCharsets.UTF_8));
            JsonPatch patch = mapper.readValue(in, JsonPatch.class);
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> personService.patchPerson("" + personOne.getId(),patch));
            assertEquals("Patch request cannot change user id.",exception.getMessage());
            verify(personRepository,never()).save(any());
        }
        @Test
        void should_throwException400BadRequest_when_patchingPerson_and_patchChangingPersonSessions() throws IOException {
            copyOfPersonOne.setEmail("zzomn@hotmail.com");

            String patchInfo = "[{ \"op\": \"remove\", \"path\": \"/yogaSessions/0\", \"value\": \"5545\" }]";
            InputStream in = new ByteArrayInputStream(patchInfo.getBytes(StandardCharsets.UTF_8));
            JsonPatch patch = mapper.readValue(in, JsonPatch.class);
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> personService.patchPerson("" + personOne.getId(),patch));
            assertEquals("Patch request cannot change user sessions.",exception.getMessage());
            verify(personRepository,never()).save(any());
        }
    }



}
