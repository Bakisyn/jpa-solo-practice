package dev.milan.jpasolopractice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.milan.jpasolopractice.model.Person;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URL;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private Person person;
    private String baseUrl;

    @BeforeEach
    void init(){
        person = new Person();
        person.setName("TestName");
        person.setAge(25);
        person.setEmail("templateEmail@hotmail.com");

        baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }


    @Nested
    class CreatingAUser{
        @Test
        void should_return_CreatedStatusWithLocation_when_correctInfo() throws Exception {

            mockMvc.perform(post(baseUrl.concat("/users/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.header().string("Location",baseUrl.concat("/users/1")));
        }

        @Test
        void should_throwApiRequestException_with400Status_when_PassedIncorrectAge() throws Exception {
            person.setAge(221);
            mockMvc.perform(post(baseUrl.concat("/users/"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void should_throwApiRequestException_with400Status_when_PassedIncorrectName() throws Exception {
            person.setName("***2");
            mockMvc.perform(post(baseUrl.concat("/users/"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
        @Test
        void should_throwApiRequestException_with400Status_when_PassedIncorrectEmail() throws Exception {
            person.setEmail("!^s2");
            mockMvc.perform(post(baseUrl.concat("/users/"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(person)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void should_throwApiRequestException_with409Status_when_PassedExistingPerson() throws Exception {
            Person person2 = new Person();
            person2.setName(person.getName());
            person2.setAge(person.getAge());
            person2.setEmail(person.getEmail());

            mockMvc.perform(post(baseUrl.concat("/users/"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(person)));
            mockMvc.perform(post(baseUrl.concat("/users/"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(person2))).andExpect(MockMvcResultMatchers.status().isConflict());
        }

    }

    public static String asJsonString(final Object obj){
        try{
            return new ObjectMapper().writeValueAsString(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
