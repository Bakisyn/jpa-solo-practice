package dev.milan.jpasolopractice.person.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.person.Person;
import dev.milan.jpasolopractice.person.PersonRepository;
import dev.milan.jpasolopractice.room.Room;
import dev.milan.jpasolopractice.shared.Patcher;
import dev.milan.jpasolopractice.yogasession.YogaSession;
import dev.milan.jpasolopractice.yogasession.util.SessionInputFormatCheckImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PersonPatcherTest {

    @MockBean
    private PersonRepository personRepository;
    @MockBean
    private SessionInputFormatCheckImpl sessionInputFormatCheckImpl;
    @MockBean
    private PersonFormatCheck personFormatCheck;
    @Autowired
    private Patcher<Person> patcher;
    @Autowired
    ObjectMapper mapper;

    private Person personOne;
    private YogaSession session;
    private List<Person> personList;
    private Person copyOfPersonOne;
    private Person testPerson;


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

        testPerson = new Person();
        testPerson.setEmail("example@hotmail.com");
        testPerson.setAge(33);
        testPerson.setName("Badji");
        testPerson.setName("Kukumber");
        testPerson.setId(4);
        testPerson.addSession(session);

    }

    @Nested
    class PatchingPerson{
        @Test
        void should_returnUpdatedPerson_when_patchingPerson_and_passedCorrectDate() throws IOException {
            personOne.setYogaSessions(new ArrayList<>());

            testPerson.setEmail("zzomn@hotmail.com");
            String patchInfo = "[{ \"op\": \"replace\", \"path\": \"/email\", \"value\": \"zzomn@hotmail.com\" }]";
            InputStream in = new ByteArrayInputStream(patchInfo.getBytes(StandardCharsets.UTF_8));
            JsonPatch patch = mapper.readValue(in, JsonPatch.class);
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            when(personRepository.save(any())).thenReturn(testPerson);
            when(sessionInputFormatCheckImpl.checkNumberFormat(anyString())).thenReturn(personOne.getId());
            when(personFormatCheck.checkPersonData(anyString(),anyInt(),anyString())).thenReturn(true);

            assertEquals(testPerson,patcher.patch(patch, personOne));
        }
        @Test
        void should_throwException400BadRequest_when_patchingPerson_and_patchChangingUserId() throws IOException {
            String patchInfo = "[{ \"op\": \"replace\", \"path\": \"/id\", \"value\": \"5545\" }]";
            InputStream in = new ByteArrayInputStream(patchInfo.getBytes(StandardCharsets.UTF_8));
            JsonPatch patch = mapper.readValue(in, JsonPatch.class);
            when(personRepository.findById(personOne.getId())).thenReturn(Optional.ofNullable(personOne));
            when(sessionInputFormatCheckImpl.checkNumberFormat(anyString())).thenReturn(personOne.getId());
            when(personFormatCheck.checkPersonData(testPerson.getName(),testPerson.getAge(),testPerson.getEmail())).thenReturn(true);
            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> patcher.patch(patch, personOne));
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
            when(sessionInputFormatCheckImpl.checkNumberFormat(anyString())).thenReturn(personOne.getId());
            when(personFormatCheck.checkPersonData(testPerson.getName(),testPerson.getAge(),testPerson.getEmail())).thenReturn(true);

            Exception exception = assertThrows(BadRequestApiRequestException.class, ()-> patcher.patch(patch, personOne));
            assertEquals("Patch request cannot change user sessions.",exception.getMessage());
            verify(personRepository,never()).save(any());
        }
    }
}
