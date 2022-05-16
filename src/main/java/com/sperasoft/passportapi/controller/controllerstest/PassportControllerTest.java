package com.sperasoft.passportapi.controller.controllerstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PassportService;
import com.sperasoft.passportapi.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class PassportControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PersonRepository personRepository;

    @MockBean
    PassportRepository passportRepository;

    @MockBean
    PersonService personService;

    @MockBean
    PassportService passportService;

    private PassportRequest passportRequest;
    private Person person;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private Passport passport;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate datePassport = LocalDate.parse("2022-05-05", format);
        passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(datePassport);
        passportRequest.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        passport = Passport.of(passportRequest);
        passportResponse = PassportResponse.of(passport);
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = Person.of(personRequest);
        personResponse = PersonResponse.of(person);

    }

    @Test
    void testFindPersonPassportsBooleanTrue() throws Exception {
        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(), "true", "", ""))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport?active=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPersonPassportsBooleanTrueWrongDates() throws Exception {
        when(passportRepository.findPassportById(passport.getId())).thenReturn(passport);
        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(),
                "true", "2022-05-06", "2022-05-05"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data"));
        this.mvc.perform(get("/person/" + personResponse.getId() +
                        "/passport?active=true&dateStart=2022-05-06&dateEnd=2022-05-05")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindPersonPassportsBooleanFalse() throws Exception {

        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(), "false", "", ""))
                .thenReturn(new ArrayList<>());
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport?active=false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testFindPersonPassportsWithoutParams() throws Exception {

        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(), "", "", ""))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testCreatePassport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(personRepository.findById(person.getId())).thenReturn(person);
        when(passportRepository.findPassportById(passport.getId())).thenReturn(passport);
        when(passportService.addPassportToPerson(personResponse.getId(), passportRequest))
                .thenReturn(passportResponse);
        String req = mapper.writer().writeValueAsString(passportResponse);
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testCreatePassportNotCorrect() throws Exception {
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport")
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindPassportWithBoolean() throws Exception {
        when(passportService.findPassportById(personResponse.getId(), passport.getId(), "true"))
                .thenReturn(passportResponse);
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId() + "?active=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPassportWithOutBoolean() throws Exception {
        when(passportService.findPassportById(personResponse.getId(), passport.getId(), ""))
                .thenReturn(passportResponse);
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }


    @Test
    void testUpdatePassportNotCorrect() throws Exception {
        this.mvc.perform(put("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePassportCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PassportResponse passportForTest = passportResponse;
        passportForTest.setDepartmentCode("111111");
        PassportRequest passportRequestTest = new PassportRequest();
        passportRequestTest.setNumber(passportForTest.getNumber());
        passportRequestTest.setGivenDate(passportForTest.getGivenDate());
        passportRequestTest.setDepartmentCode(passportForTest.getDepartmentCode());
        when(passportService.findPassportById(personResponse.getId(), passport.getId(), "true"))
                .thenReturn(passportResponse);
        when(passportService.updatePassport(personResponse.getId(), passport.getId(), passportRequestTest))
                .thenReturn(passportForTest);
        String req = mapper.writer().writeValueAsString(passportForTest);
        this.mvc.perform(put("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("111111")));
    }

    @Test
    void deletePassport() throws Exception {
        when(personRepository.findById(person.getId())).thenReturn(person);
        when(passportRepository.findPassportById(passport.getId())).thenReturn(passport);
        when(passportService.deletePassport(personResponse.getId(), passport.getId()))
                .thenReturn(passportResponse);
        this.mvc.perform(delete("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}