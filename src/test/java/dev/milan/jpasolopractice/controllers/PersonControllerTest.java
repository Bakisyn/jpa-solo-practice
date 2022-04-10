package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.milan.jpasolopractice.customException.ApiRequestException;
import dev.milan.jpasolopractice.model.Person;
import dev.milan.jpasolopractice.model.Room;
import dev.milan.jpasolopractice.model.YogaSession;
import dev.milan.jpasolopractice.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private Person person;
    private String baseUrl;

    @MockBean
    private PersonService personService;

    @BeforeEach
    void init(){
        person = new Person();
        person.setName("TestName");
        person.setAge(25);
        person.setEmail("templateEmail@hotmail.com");
        person.setId(1);

        baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }


    @Nested
    class CreatingAUser{
        @Test
        void should_return_CreatedStatusWithLocation_when_correctInfo() throws Exception {
            when(personService.addPerson(anyString(),anyInt(),anyString())).thenReturn(person);

            mockMvc.perform(post(baseUrl.concat("/users/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.header().string("Location",baseUrl.concat("/users/1")));
        }

        @Test
        void should_throwApiRequestException_with400Status_when_PassedIncorrectInfo() throws Exception {
            when(personService.addPerson(anyString(),anyInt(),anyString())).thenThrow(new ApiRequestException("Couldn't create person because of bad info./400"));
            mockMvc.perform(post(baseUrl.concat("/users/"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void should_throwApiRequestException_with409Status_when_PassedExistingPerson() throws Exception {
            when(personService.addPerson(anyString(),anyInt(),anyString())).thenThrow(new ApiRequestException("Person already exists./409"));
            mockMvc.perform(post(baseUrl.concat("/users/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(person))).andExpect(MockMvcResultMatchers.status().isConflict());
        }

    }
    @Nested
    class SearchingForUsers{
        @Test
        void should_returnUser_when_userFoundById() throws Exception {
            when(personService.findPersonById(anyInt())).thenReturn(person);

            mockMvc.perform(get(baseUrl.concat("/users/1"))).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().json(asJsonString(person)));
        }

        @Test
        void should_returnApiRequestException404_when_userNotFoundById() throws Exception {
            when(personService.findPersonById(12)).thenThrow(new ApiRequestException("Person with that id couldn't be found./404"));
            mockMvc.perform(get(baseUrl.concat("/users/12")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_returnAListOfPeople_when_searchingByName() throws Exception {
            List<Person> people = new ArrayList<>();
            Person person1 = new Person();
            person1.setName("NameOne");
            person1.setAge(26);
            person1.setEmail("personOne@gmail.com");
            Person person2 = new Person();
            person2.setName("NameOne");
            person2.setAge(27);
            person2.setEmail("personTwo@hotmail.com");
            people.add(person1);
            people.add(person2);

            when(personService.findPeopleByName(any())).thenReturn(people);

            mockMvc.perform(get(baseUrl.concat("/users?name=NameOne")))
                    .andExpect(status().isOk())
                    .andExpect(content().json(asJsonString(people)));
        }

        @Test
        void should_returnApiRequestException404_when_usersNotFoundByName() throws Exception {
            when(personService.findPeopleByName(anyString())).thenThrow(new ApiRequestException("People with that name couldn't be found./404"));
            mockMvc.perform(get(baseUrl.concat("/users?name=petar")))
                    .andExpect(status().isNotFound());
        }

    }


    @Nested
    class RemovingSessionFromPerson{
        @Test
        void should_returnOkStatusWithPersonLocationHeader_when_successfullyRemovedSessionFromUser() throws Exception {
            when(personService.removeSessionFromPerson(anyInt(),anyInt())).thenReturn(true);

            mockMvc.perform(patch(baseUrl.concat("/users/1/sessions/2")))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.header().string("Location",baseUrl.concat("/users/1")));
        }
        @Test
        void should_returnBadRequestStatusWithMessage_when_personDoesntContainSession() throws Exception {
            when(personService.removeSessionFromPerson(anyInt(),anyInt())).thenReturn(false);

            mockMvc.perform(patch(baseUrl.concat("/users/1/sessions/2")))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(content().string("Yoga session was not found in user's yoga sessions."));
        }
        @Test
        void should_return404NotFoundStatusAndMessage_when_removingSessionFromAPersonWhoDoesntExist() throws Exception {
            when(personService.removeSessionFromPerson(anyInt(),anyInt())).thenThrow(new ApiRequestException("Person with that id couldn't be found./404"));

            mockMvc.perform(patch(baseUrl.concat("/users/1/sessions/2")))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Person with that id couldn't be found."));
        }
        @Test
        void should_return404NotFoundStatusAndMessage_when_removingSessionFromAPersonWhenSessionDoesntExist() throws Exception {
            when(personService.removeSessionFromPerson(anyInt(),anyInt())).thenThrow(new ApiRequestException("Yoga session with that id couldn't be found./404"));

            mockMvc.perform(patch(baseUrl.concat("/users/1/sessions/2")))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Yoga session with that id couldn't be found."));
        }
    }

    @Test
    void should_returnAllPersonSessions_when_personExists() throws Exception {
        List<YogaSession> sessionsList = new ArrayList<>();
        YogaSession session1 = new YogaSession();
        session1.setId(1);
        session1.setRoom(new Room());
        session1.setDate(LocalDate.now().plusDays(20));
        session1.setStartOfSession(LocalTime.of(12,00,00));
        YogaSession session2 = new YogaSession();
        session2.setId(2);
        session2.setRoom(new Room());
        session2.setDate(LocalDate.now().plusDays(20));
        session2.setStartOfSession(LocalTime.of(14,00,00));
        sessionsList.add(session1);
        sessionsList.add(session2);
        when(personService.getAllSessionsFromPerson(anyInt())).thenReturn(sessionsList);

        mockMvc.perform(get(baseUrl.concat("/users/1/sessions")))
                .andExpect(content().string(asJsonString(sessionsList)));
    }


    public static String asJsonString(final Object obj){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");
            objectMapper.setDateFormat(df);
            return objectMapper.registerModule(new JavaTimeModule()).writeValueAsString(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
