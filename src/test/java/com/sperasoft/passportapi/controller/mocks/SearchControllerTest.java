package com.sperasoft.passportapi.controller.mocks;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
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

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment environment;

    @MockBean
    private SearchController searchController;

    private Person person;
    private PersonResponse personResponse;
    private PassportResponse passportResponse;
    private Passport passport;
    private final ObjectMapper mapper = new ObjectMapper();


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
        passport = Passport.of(FriendlyId.createFriendlyId(), passportRequest);
        person = Person.of(FriendlyId.createFriendlyId(), personRequest);
        personResponse = PersonResponse.of(person);
        passportResponse = PassportResponse.of(passport);
    }

    @Test
    void testFindPersonByPassportNumberCorrect() throws Exception {
        NumberPassport numberPassport = new NumberPassport();
        numberPassport.setNumber("1223123113");
        when(searchController.findPersonByPassportNumber(numberPassport)).thenReturn(personResponse);

        String req = mapper.writer().writeValueAsString(numberPassport);
        this.mvc.perform(post("/searches")
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
        this.mvc.perform(post("/searches")
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
        this.mvc.perform(get("/searches")
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindAllPassportsCorrectBadDates() throws Exception {
        when(searchController.findAllPassports(null, Instant.parse("2022-05-15T19:00:00+02:00"),
                Instant.parse("2022-05-10T19:00:00+03:00")))
                .thenThrow(new InvalidPassportDataException());

        this.mvc.perform(get("/searches?dateStart=2022-05-15T19:00:00+02:00&dateEnd=2022-05-10T19:00:00+03:00")
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString().equals(
                        environment.getProperty("exception.InvalidPassportDataException")));
    }
}