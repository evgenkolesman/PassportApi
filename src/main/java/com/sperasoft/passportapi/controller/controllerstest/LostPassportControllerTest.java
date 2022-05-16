package com.sperasoft.passportapi.controller.controllerstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class LostPassportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PersonRepository personRepository;

    private Person person;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate dateToday = LocalDate.now();
        PassportRequest passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(dateToday);
        passportRequest.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        Passport passport = Passport.of(passportRequest);
        passportResponse = PassportResponse.of(passport);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = new Person();
             person =    person.of(personRequest);
        personResponse = PersonResponse.of(person);
        person.getList().add(passport);

    }


    @Test
    void lostPassportDeactivate() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(personRepository.findById(person.getId())).thenReturn(person);
        String req = mapper.writer().writeValueAsString(new Description());
        this.mvc.perform(post("/person/" + personResponse.getId()
                        + "/passport/" + passportResponse.getId() + "/lostPassport?active=false")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void lostPassportDeactivateConflict() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        person.getList().get(0).setActive(false);
        when(personRepository.findById(person.getId())).thenReturn(person);
        String req = mapper.writer().writeValueAsString(new Description());
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport/"
                        + passportResponse.getId() + "/lostPassport?active=false")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isConflict());
    }
}