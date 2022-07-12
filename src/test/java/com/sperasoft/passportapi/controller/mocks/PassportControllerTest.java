package com.sperasoft.passportapi.controller.mocks;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.PassportController;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.LostPassportInfo;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
class PassportControllerTest {

    private static final String HTTP_LOCALHOST = "http://localhost";
    private static final String PERSON_ENDPOINT = "/person";
    private static final String PASSPORT_ENDPOINT = "/passport";
    private static final String LOST_PASSPORT_ENDPOINT = "/lostPassport";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PassportController passportController;

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectMapper mapper;

    private PassportRequest passportRequest;
    private PassportResponse passportResponse;
    private Person person;
    private Passport passport;
    DateTimeFormatter isoOffsetDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @BeforeEach
    private void testDataProduce() {

        RestAssuredMockMvc.mockMvc(mvc);
        String string = "2010-02-02";
        LocalDate dateToday = LocalDate.now();
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        passportRequest = new PassportRequest("1223123113",
                dateToday.atStartOfDay().toInstant(ZoneOffset.MIN),
                "123123");
        PersonRequest personRequest = new PersonRequest("Alex Frolov",
                date,
                "UK");
        person = new Person(FriendlyId.createFriendlyId(),
                personRequest.getName(), personRequest.getBirthday(),
                personRequest.getBirthdayCountry());
        passport = new Passport(FriendlyId.createFriendlyId(),
                person.getId(),
                passportRequest.getNumber(),
                passportRequest.getGivenDate(),
                passportRequest.getDepartmentCode());
        passportResponse = PassportResponse.of(passport);
    }

    @Test
    void testFindPersonPassportsBooleanTrue() throws Exception {
        when(passportController.findPersonPassports(person.getId(), true, null, null))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST)
                        .path(PERSON_ENDPOINT)
                        .path("/")
                        .path(person.getId())
                        .path(PASSPORT_ENDPOINT).queryParam("active", "true").toUriString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPersonPassportsBooleanTrueWrongDates() throws Exception {
        when(passportController.findPersonPassports(person.getId(),
                true, Instant.from(isoOffsetDateTime.parse("2022-05-06T19:00:00-02:00")),
                Instant.from(isoOffsetDateTime.parse("2022-05-05T19:00:00-02:00"))))
                .thenThrow(new InvalidPassportDataException());
        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .queryParam("active", "true")
                        .queryParam("dateStart", "2022-05-06T19:00:00-02:00")
                        .queryParam("dateEnd", "2022-05-05T19:00:00-02:00").toUriString())
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
                true, Instant.from(isoOffsetDateTime.parse("2022-05-02T19:00:00-02:00")),
                Instant.from(isoOffsetDateTime.parse("2022-05-08T19:00:00-02:00"))))
                .thenThrow(new PassportEmptyException(person.getId()));
        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).replacePath(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .queryParam("active", "true")
                        .queryParam("dateStart", "2022-05-02T19:00:00-02:00")
                        .queryParam("dateEnd", "2022-05-08T19:00:00-02:00").toUriString())
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
        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST)
                        .path(PERSON_ENDPOINT)
                        .path("/")
                        .path(person.getId())
                        .path(PASSPORT_ENDPOINT).queryParam("active", "false").toUriString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testFindPersonPassportsWithoutParams() throws Exception {
        when(passportController.findPersonPassports(person.getId(), null, null, null))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST)
                        .path(PERSON_ENDPOINT)
                        .path("/")
                        .path(person.getId())
                        .path(PASSPORT_ENDPOINT).toUriString())
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
        this.mvc.perform(post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST)
                        .path(PERSON_ENDPOINT)
                        .path("/")
                        .path(person.getId())
                        .path(PASSPORT_ENDPOINT).toUriString())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testCreatePassportNotCorrect() throws Exception {
        this.mvc.perform(post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST)
                        .path(PERSON_ENDPOINT)
                        .path("/")
                        .path(person.getId())
                        .path(PASSPORT_ENDPOINT).toUriString())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePassportNotCorrectBadData() throws Exception {
        PassportRequest passportRequest1 = new PassportRequest(
                String.valueOf(ThreadLocalRandom.current().nextInt(999999999) + 1000000000),
                passportRequest.getGivenDate(),
                passportRequest.getDepartmentCode());

        when(passportController.createPassport(person.getId(), passportRequest1))
                .thenThrow(new PassportWasAddedException());
        String req = mapper.writer().writeValueAsString(passportRequest1);
        this.mvc.perform(post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST)
                        .path(PERSON_ENDPOINT)
                        .path("/")
                        .path(person.getId())
                        .path(PASSPORT_ENDPOINT).toUriString())
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
        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId())
                        .queryParam("active", "true").toUriString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPassportWithOutBoolean() throws Exception {
        when(passportController.findPassport(passport.getId(), null))
                .thenReturn(passportResponse);
        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId()).toUriString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }


    @Test
    void testUpdatePassportNotCorrect() throws Exception {
        this.mvc.perform(put(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId()).toUriString())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePassportNotCorrectNotFound() throws Exception {
        PassportRequest passportForTest = new PassportRequest(passportRequest.getNumber(),
                passportRequest.getGivenDate(),
                "111111");
        when(passportController.updatePassport(person.getId(), person.getId(), passportForTest))
                .thenThrow(new PersonNotFoundException(person.getId()));
        String req = mapper.writer().writeValueAsString(passportForTest);
        this.mvc.perform(put(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(person.getId()).toUriString())
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
        PassportRequest passportForTest = new PassportRequest(passportRequest.getNumber(),
                passportRequest.getGivenDate(),
                "111111");
        Passport passport1 = new Passport(FriendlyId.createFriendlyId(),
                        person.getId(),
                        passportForTest.getNumber(),
                        passportForTest.getGivenDate(),
                        passportForTest.getDepartmentCode());
        when(passportController.updatePassport(person.getId(), passport.getId(), passportForTest))
                .thenReturn(PassportResponse.of(passport1));
        String req = mapper.writer().writeValueAsString(passportForTest);
        this.mvc.perform(put(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId()).toUriString())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("111111")));
    }

    @Test
    void deletePassport() throws Exception {
        this.mvc.perform(delete(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId()).toUriString())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePassportNotCorrect() throws Exception {
        doThrow(new PassportNotFoundException(passport.getId())).when(passportController).deletePassport(passport.getId());
        this.mvc.perform(delete(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId()).toUriString())
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
                passportResponse.getId(), new LostPassportInfo("new Desc")))
                .thenReturn(true);
        String req = mapper.writer().writeValueAsString("new Desc");
        this.mvc.perform(post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId())
                        .path(LOST_PASSPORT_ENDPOINT)
                        .queryParam("active", "false").toUriString())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void lostPassportDeactivateConflict() throws Exception {
        when(passportController.lostPassportDeactivate(person.getId(), passport.getId(), new LostPassportInfo("new Desc")))
                .thenThrow(new PassportDeactivatedException());
        String req = mapper.writer().writeValueAsString("new Desc");
        this.mvc.perform(post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).path(PERSON_ENDPOINT)
                        .path("/").path(person.getId())
                        .path(PASSPORT_ENDPOINT)
                        .path("/").path(passportResponse.getId())
                        .path(LOST_PASSPORT_ENDPOINT)
                        .queryParam("active", "false").toUriString())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(environment.getProperty("exception.PassportDeactivatedException")));
    }
}