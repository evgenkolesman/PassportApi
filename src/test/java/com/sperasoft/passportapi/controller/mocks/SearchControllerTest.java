package com.sperasoft.passportapi.controller.mocks;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.SearchController;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWrongNumberException;
import com.sperasoft.passportapi.model.NumberPassport;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    private static final String SEARCHES_ENDPOINT = "/searches";
    private static final String HTTP_LOCALHOST = "http://localhost";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private SearchController searchController;

    private Person person;
    private PersonResponse personResponse;
    private PassportResponse passportResponse;
    private Passport passport;


    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate dateToday = LocalDate.now();
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        PassportRequest passportRequest = new PassportRequest("1223123113",
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

        personResponse = PersonResponse.of(person);
        passportResponse = PassportResponse.of(passport);
    }

    @Test
    void testFindPersonByPassportNumberCorrect() throws Exception {
        NumberPassport numberPassport = new NumberPassport();
        numberPassport.setNumber("1223123113");
        when(searchController.findPersonByPassportNumber(numberPassport)).thenReturn(personResponse);

        String req = mapper.writer().writeValueAsString(numberPassport);
        this.mvc.perform(post(SEARCHES_ENDPOINT)
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }


    @Test
    void testFindPersonByPassportNumberNotCorrect() throws Exception {
        NumberPassport number = new NumberPassport();
        number.setNumber("2313");
        when(searchController.findPersonByPassportNumber(number)).thenThrow(new PassportWrongNumberException());
        String req = mapper.writer().writeValueAsString(number);
        this.mvc.perform(post(SEARCHES_ENDPOINT)
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString().
                        equals(environment.getProperty("exception.PassportWrongNumberException")));

    }

    @Test
    void testFindAllPassportsCorrect() throws Exception {
        when(searchController.findAllPassports(null, null, null)).thenReturn(List.of(passportResponse));
        this.mvc.perform(get(SEARCHES_ENDPOINT)
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindAllPassportsCorrectBadDates() throws Exception {
        var startDate = "2022-05-15T19:00:00+02:00";
        var endDate = "2022-05-10T19:00:00+03:00";
        when(searchController.findAllPassports(null,
                Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(startDate)),
                Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(endDate))))
                .thenThrow(new InvalidPassportDataException());

        this.mvc.perform(get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST)
                        .path(SEARCHES_ENDPOINT)
                        .queryParam("dateStart", startDate)
                        .queryParam("dateEnd", endDate).toUriString())
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString().equals(
                        environment.getProperty("exception.InvalidPassportDataException")));
    }
}