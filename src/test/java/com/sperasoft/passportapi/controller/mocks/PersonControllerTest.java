package com.sperasoft.passportapi.controller.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.PersonController;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment environment;

    @MockBean
    private PersonController personController;

    private PersonResponse personResponse;
    private PersonRequest personRequest;
    private Person person;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(Instant.now());
        passport.setDepartmentCode("123123");
        personRequest = new PersonRequest();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = Person.of(personRequest);
        personResponse = PersonResponse.of(person);
    }

    @Test
    void testCreatePersonCorrect() throws Exception {
        when(personController.createPerson(personRequest)).thenReturn(personResponse);
        String req = mapper.writeValueAsString(personRequest);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testCreatePersonNotCorrect() throws Exception {
        when(personController.createPerson(personRequest)).thenThrow(new InvalidPersonDataException());
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(environment.getProperty("exception.InvalidPersonDataException")));
    }

    @Test
    void testFindPersonById() throws Exception {
        when(personController.findPersonById(person.getId())).thenReturn(personResponse);
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(get("/person/" + person.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testFindPersonByIdNotCorrect() throws Exception {
        String id = "23";
        when(personController.findPersonById(id))
                .thenThrow(new PersonNotFoundException(id));
        this.mvc.perform(get("/person/" + id).contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(
                                environment.getProperty("exception.PersonNotFoundException"), id)));
    }

    @Test
    void testUpdatePersonCorrect() throws Exception {
        when(personController.updatePerson(person.getId(), personRequest)).thenReturn(personResponse);
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(put("/person/" + person.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(personResponse.getName())));
    }

    @Test
    void testUpdatePersonNotCorrect() throws Exception {
        String req = mapper.writer().writeValueAsString("");
        this.mvc.perform(put("/person/" + person.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePersonCorrect() throws Exception {
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(delete("/person/" + person.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePersonNotCorrect() throws Exception {
        String id = "23";
        doThrow(new PersonNotFoundException(id)).when(personController).deletePerson(id);
        this.mvc.perform(delete("/person/" + id).contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(environment.getProperty("exception.PersonNotFoundException"), id)));
    }
}