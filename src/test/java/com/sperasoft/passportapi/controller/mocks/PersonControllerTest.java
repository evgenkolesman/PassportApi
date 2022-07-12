package com.sperasoft.passportapi.controller.mocks;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sperasoft.passportapi.controller.PersonController;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerTest {

    private static final String PERSON_ENDPOINT = "/person";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment environment;

    @MockBean
    private PersonController personController;

    @Autowired
    private ObjectMapper mapper;


    private PersonResponse personResponse;
    private PersonRequest personRequest;
    private Person person;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        personRequest = new PersonRequest("Alex Frolov",
                date,
                "UK");
        person = new Person(FriendlyId.createFriendlyId(),
                personRequest.getName(), personRequest.getBirthday(),
                personRequest.getBirthdayCountry());
        personResponse = PersonResponse.of(person);
    }

    @Test
    void testCreatePersonCorrect() throws Exception {

//        this.mvc.perform(post(PERSON_ENDPOINT).contentType("application/json")
//                        .content(req))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testCreatePersonNotCorrect() throws Exception {
        when(personController.createPerson(personRequest)).thenThrow(new InvalidPersonDataException());
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(post(PERSON_ENDPOINT).contentType("application/json")
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
        this.mvc.perform(get(PERSON_ENDPOINT + "/" + person.getId()).contentType("application/json")
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
        this.mvc.perform(get(PERSON_ENDPOINT + id).contentType("application/json")
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
        this.mvc.perform(put(PERSON_ENDPOINT + "/" + person.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(personResponse.getName())));
    }

    @Test
    void testUpdatePersonNotCorrect() throws Exception {
        String req = mapper.writer().writeValueAsString("");
        this.mvc.perform(put(PERSON_ENDPOINT + "/" + person.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePersonCorrect() throws Exception {
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(delete(PERSON_ENDPOINT + "/" + person.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePersonNotCorrect() throws Exception {
        String id = "23";
        doThrow(new PersonNotFoundException(id)).when(personController).deletePerson(id);
        this.mvc.perform(delete(PERSON_ENDPOINT + "/" + id).contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(environment.getProperty("exception.PersonNotFoundException"), id)));
    }
}