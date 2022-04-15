package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.milan.jpasolopractice.customException.differentExceptions.BadRequestApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.ConflictApiRequestException;
import dev.milan.jpasolopractice.customException.differentExceptions.NotFoundApiRequestException;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private Person person;
    private String baseUrl;
    private int personId;
    private int sessionId;
    private List<Person> personList;

    @MockBean
    private PersonService personService;


    @BeforeEach
    void init(){
        person = new Person();
        person.setName("TestName");
        person.setAge(25);
        person.setEmail("templateEmail@hotmail.com");
        person.setId(1);


        personId = 1;
        sessionId = 2;
        baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        personList = new ArrayList<>();
        personList.add(person);
    }


    @Nested
    class CreatingAUser{
        @Test
        void should_returnCreatedStatusWithLocation_when_correctInfo() throws Exception {
            when(personService.addPerson(anyString(),anyInt(),anyString())).thenReturn(person);

            mockMvc.perform(post(baseUrl.concat("/users/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.header().string("Location",baseUrl.concat("/users/1")));
        }

        @Test
        void should_throwException400BadRequestWithMessage_when_passedIncorrectInfo() throws Exception {
            when(personService.addPerson(anyString(),anyInt(),anyString())).thenThrow(new BadRequestApiRequestException("Couldn't create person because of bad info."));
            mockMvc.perform(post(baseUrl.concat("/users/"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Couldn't create person because of bad info."));
        }

        @Test
        void should_throwException409ConflictWithMessage_when_passedExistingPerson() throws Exception {
            when(personService.addPerson(anyString(),anyInt(),anyString())).thenThrow(new ConflictApiRequestException("Person already exists."));
            mockMvc.perform(post(baseUrl.concat("/users/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(person))).andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(jsonPath("$.message").value("Person already exists."));
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
        void should_throwException404NotFoundWithMessage_when_userNotFoundById() throws Exception {
            when(personService.findPersonById(personId)).thenThrow(new NotFoundApiRequestException("Person id:" + personId + " couldn't be found."));

            mockMvc.perform(get(baseUrl.concat("/users/" + personId)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Person id:" + personId + " couldn't be found."));
        }

        @Test
        void should_returnPersonList_when_searchingByParams_and_noParamsPassed() throws Exception {
            when(personService.findPeopleByParams(Optional.empty(),Optional.empty(),Optional.empty())).thenReturn(personList);
            mockMvc.perform(get(baseUrl.concat("/users"))).andExpect(content().string(asJsonString(personList)));
        }
        @Test
        void should_returnPersonList_when_searchingByParams_and_sessionIdPassed() throws Exception {
            when(personService.findPeopleByParams(Optional.of("" + sessionId),Optional.empty(),Optional.empty())).thenReturn(personList);
            mockMvc.perform(get(baseUrl.concat("/users?sessionId=" + sessionId))).andExpect(content().string(asJsonString(personList)));
        }
        @Test
        void should_returnPersonList_when_searchingByParams_and_startAgeEndAgeSessionIdPassed() throws Exception {
            when(personService.findPeopleByParams(Optional.of("" + sessionId),Optional.of("33"),Optional.of("34"))).thenReturn(personList);
            mockMvc.perform(get(baseUrl.concat("/users?sessionId=" + sessionId + "&startAge="+ 33 + "&endAge="+34))).andExpect(content().string(asJsonString(personList)));
        }
        @Test
        void should_returnPersonList_when_searchingByParams_and_startAgeEndAgePassed() throws Exception {
            when(personService.findPeopleByParams(Optional.empty(),Optional.of("33"),Optional.of("34"))).thenReturn(personList);
            mockMvc.perform(get(baseUrl.concat("/users?startAge="+ 33 + "&endAge="+34))).andExpect(content().string(asJsonString(personList)));
        }


        @Test
        void should_throwExceptionWhenServiceThrows_when_searchingByParams() throws Exception {
            when(personService.findPeopleByParams(Optional.empty(),Optional.empty(),Optional.empty())).thenThrow(new BadRequestApiRequestException("Number must be an integer value."));
            mockMvc.perform(get(baseUrl.concat("/users"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Number must be an integer value."));
        }


    }


    @Test
    void should_returnAllPersonSessions_when_personExists() throws Exception {
        List<YogaSession> sessionsList = new ArrayList<>();
        YogaSession session1 = new YogaSession();
        session1.setId(1);
        session1.setRoom(new Room());
        session1.setDate(LocalDate.now().plusDays(20));
        session1.setStartOfSession(LocalTime.of(12,0,0));
        YogaSession session2 = new YogaSession();
        session2.setId(2);
        session2.setRoom(new Room());
        session2.setDate(LocalDate.now().plusDays(20));
        session2.setStartOfSession(LocalTime.of(14,0,0));
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
