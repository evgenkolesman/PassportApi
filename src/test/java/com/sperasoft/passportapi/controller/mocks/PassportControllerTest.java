package com.sperasoft.passportapi.controller.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.PassportController;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class PassportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PassportController passportController;

    @Autowired
    private Environment environment;

    private PassportRequest passportRequest;
    private PassportResponse passportResponse;
    private Person person;
    private Passport passport;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate datePassport = LocalDate.parse("2022-05-05", format);
        passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(datePassport.atStartOfDay().toInstant(ZoneOffset.MIN));
        passportRequest.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        passport = Passport.of(passportRequest);
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = Person.of(personRequest);
        passportResponse = PassportResponse.of(passport);
    }

    @Test
    void testFindPersonPassportsBooleanTrue() throws Exception {
        when(passportController.findPersonPassports(person.getId(), true, null, null))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/person/" + person.getId() + "/passport?active=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPersonPassportsBooleanTrueWrongDates() throws Exception {
        when(passportController.findPersonPassports(person.getId(),
                true, Instant.parse("2022-05-06T19:00:00-02:00"), Instant.parse("2022-05-05T19:00:00-02:00")))
                .thenThrow(new InvalidPassportDataException());
        this.mvc.perform(get("/person/" + person.getId() +
                        "/passport?active=true&dateStart=2022-05-06T19:00:00-02:00&dateEnd=2022-05-05T19:00:00-02:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(
                                        environment.getProperty("exception.InvalidPassportDataException"),
                                person.getId())));
    }

    @Test
    void testFindPersonPassportsWithoutPassport() throws Exception {
        when(passportController.findPersonPassports(person.getId(),
                true, Instant.parse("2022-05-02T19:00:00-02:00"), Instant.parse("2022-05-08T19:00:00-02:00")))
                .thenThrow(new PassportEmptyException(person.getId()));
        this.mvc.perform(get("/person/" + person.getId() +
                        "/passport?active=true&dateStart=2022-05-02T19:00:00-02:00&dateEnd=2022-05-08T19:00:00-02:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(
                                environment.getProperty("exception.PassportEmptyException"),
                                person.getId())));
    }

    @Test
    void testFindPersonPassportsBooleanFalse() throws Exception {
        when(passportController.findPersonPassports(person.getId(), false, null, null))
                .thenReturn(new ArrayList<>());
        this.mvc.perform(get("/person/" + person.getId() + "/passport?active=false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testFindPersonPassportsWithoutParams() throws Exception {
        when(passportController.findPersonPassports(person.getId(), null, null, null))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/person/" + person.getId() + "/passport")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testCreatePassport() throws Exception {
        when(passportController.createPassport(person.getId(), passportRequest))
                .thenReturn(passportResponse);
        String req = mapper.writer().writeValueAsString(passportRequest);
        this.mvc.perform(post("/person/" + person.getId() + "/passport")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testCreatePassportNotCorrect() throws Exception {
        this.mvc.perform(post("/person/" + person.getId() + "/passport")
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePassportNotCorrectBadData() throws Exception {
        PassportRequest passportRequest1 = passportRequest;
        passportRequest1.setNumber("233");
        when(passportController.createPassport(person.getId(), passportRequest1))
                .thenThrow(new PassportWasAddedException());
        String req = mapper.writer().writeValueAsString(passportRequest1);
        this.mvc.perform(post("/person/" + person.getId() + "/passport")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(environment.getProperty("exception.PassportWasAddedException")));
    }

    @Test
    void testFindPassportWithBoolean() throws Exception {
        when(passportController.findPassport(passport.getId(), true))
                .thenReturn(passportResponse);
        this.mvc.perform(get("/person/" + person.getId() + "/passport/" +
                        passport.getId() + "?active=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPassportWithOutBoolean() throws Exception {
        when(passportController.findPassport(passport.getId(), null))
                .thenReturn(passportResponse);
        this.mvc.perform(get("/person/" + person.getId() + "/passport/" + passport.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }


    @Test
    void testUpdatePassportNotCorrect() throws Exception {
        this.mvc.perform(put("/person/" + person.getId() + "/passport/" + passport.getId())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePassportNotCorrectNotFound() throws Exception {
        PassportRequest passportForTest = passportRequest;
        passportForTest.setDepartmentCode("111111");
        when(passportController.updatePassport(person.getId(), passportForTest))
                .thenThrow(new PersonNotFoundException(person.getId()));
        String req = mapper.writer().writeValueAsString(passportForTest);
        this.mvc.perform(put("/person/" + person.getId() + "/passport/" + person.getId())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect((a) ->
                        a.getResponse().getContentAsString()
                                .equals((String.format(
                                                environment.getProperty("exception.PersonNotFoundException"),
                                        person.getId()))));
    }

    @Test
    void testUpdatePassportCorrect() throws Exception {
        PassportRequest passportForTest = passportRequest;
        passportForTest.setDepartmentCode("111111");
        Passport passport1 = Passport.of(passportForTest);
        when(passportController.updatePassport(passport.getId(), passportForTest))
                .thenReturn(PassportResponse.of(passport1));
        String req = mapper.writer().writeValueAsString(passportForTest);
        this.mvc.perform(put("/person/" + person.getId() + "/passport/" + passport.getId())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("111111")));
    }

    @Test
    void deletePassport() throws Exception {
        this.mvc.perform(delete("/person/" + person.getId() + "/passport/" + passport.getId())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePassportNotCorrect() throws Exception {
        doThrow(new PassportNotFoundException(passport.getId())).when(passportController).deletePassport(passport.getId());
        this.mvc.perform(delete("/person/" + person.getId() + "/passport/" + passport.getId())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(
                                environment.getProperty("exception.PassportNotFoundException"),
                                passport.getId())
                ));
    }

    @Test
    void lostPassportDeactivate() throws Exception {
        when(passportController.lostPassportDeactivate(passportResponse.getId(),
                passportResponse.getId(), false, new Description()))
                .thenReturn(true);
        String req = mapper.writer().writeValueAsString(new Description());
        this.mvc.perform(post("/person/" + person.getId()
                        + "/passport/" + passport.getId() + "/lostPassport?active=false")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void lostPassportDeactivateConflict() throws Exception {
        when(passportController.lostPassportDeactivate(person.getId(), passport.getId(), false, new Description()))
                .thenThrow(new PassportDeactivatedException());
        String req = mapper.writer().writeValueAsString(new Description());
        this.mvc.perform(post("/person/" + person.getId() + "/passport/"
                        + passport.getId() + "/lostPassport?active=false")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(environment.getProperty("exception.PassportDeactivatedException")));
    }
}