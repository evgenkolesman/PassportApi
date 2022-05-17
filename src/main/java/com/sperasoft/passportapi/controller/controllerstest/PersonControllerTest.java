package com.sperasoft.passportapi.controller.controllerstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PersonRepository personRepository;

    @MockBean
    PassportRepository passportRepository;

    @MockBean
    PersonService personService;

    private PersonRequest personRequest;
    private Person person;
    private PersonResponse personResponse;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate dateToday = LocalDate.now();
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(dateToday);
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
        ObjectMapper mapper = new ObjectMapper();
        when(personService.addPerson(personRequest)).thenReturn(personResponse);
        String req = mapper.writeValueAsString(personResponse);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testCreatePersonNotCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PersonResponse personResponseForTest = personResponse;
        personResponseForTest.setName("");
        PersonRequest personRequestForTest = personRequest;
        personRequestForTest.setName("");
        when(personRepository.isPersonPresent(personRequestForTest)).thenReturn(true);
        when(personService.addPerson(personRequestForTest))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data"));
        String req = mapper.writer().writeValueAsString(null);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().is4xxClientError());

    }

    @Test
    void testFindPersonById() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(personService.findById(personResponse.getId())).thenReturn(personResponse);
        String req = mapper.writer().writeValueAsString(personResponse);
        this.mvc.perform(get("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testFindPersonByIdNotCorrect() throws Exception {
        when(personService.findById("23"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found"));
        this.mvc.perform(get("/person/23").contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePersonCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PersonRequest personRequestForTest = personRequest;
        personRequestForTest.setName("AAAAAAA");
        Person personNew= Person.of(personRequest);
        PersonResponse personResponseForTest = PersonResponse.of(personNew);

        when(personRepository.findPersonById(personResponseForTest.getId())).thenReturn(personNew);
        when(personService.findById(personResponse.getId())).thenReturn(personResponseForTest);
        when(personService.updatePerson(person.getId(), personRequestForTest)).thenReturn(personResponseForTest);
        String req = mapper.writer().writeValueAsString(personResponseForTest);
        this.mvc.perform(put("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AAAAAAA")));
    }

    @Test
    void testUpdatePersonNotCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String req = mapper.writer().writeValueAsString("");
        this.mvc.perform(put("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePersonCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String req = mapper.writer().writeValueAsString(personResponse);
        this.mvc.perform(delete("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(containsString("")));
    }

    @Test
    void testDeletePersonNotCorrect() throws Exception {
        when(personService.deletePerson("23"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid data"));
        this.mvc.perform(delete("/person/23").contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}