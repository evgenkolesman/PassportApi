package com.sperasoft.passportapi.controller.searchtest;


import com.sperasoft.passportapi.controller.abstracts.PassportTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.SearchTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.TestAbstractIntegration;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.ErrorModel;
import com.sperasoft.passportapi.model.Number;
import com.sperasoft.passportapi.repository.PassportRepository;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchFindAllPassportTest extends TestAbstractIntegration {


    @Autowired
    private Environment env;

    @Autowired
    private PersonTestMethodContainer personAbstract;

    @Autowired
    private PassportTestMethodContainer passportAbstract;

    @Autowired
    private SearchTestMethodContainer searchAbstract;

    @LocalServerPort
    private int port;

    @Autowired
    private PassportRepository passportRepository;


    private PassportResponse passportResponse;
    private PersonResponse personResponse;

    private Long number;


    @BeforeEach
    void testDataProduce() throws Exception {
        RestAssured.port = port;

        number = ThreadLocalRandom.current().nextLong(899999999) + 1000000000;
        int varInt = ThreadLocalRandom.current().nextInt(10000000);
        PassportRequest passportRequest = new PassportRequest(
                String.valueOf(number),
                LocalDate.of(2022, 5, 5).atStartOfDay().toInstant(ZoneOffset.MIN),
                String.valueOf(ThreadLocalRandom.current().nextInt(899999) + 100000));
        PersonRequest personRequest = new PersonRequest(
                "Alex Frolov" + varInt,
                LocalDate.of(2022, 5, 5),
                "UK");
        personResponse = personAbstract.createPerson(personRequest)
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        passportResponse = passportAbstract.createPassport(personResponse.getId(), passportRequest)
                .assertThat().statusCode(200)
                .extract().body().as(PassportResponse.class);
    }

    @AfterEach
    public void testDataClear() {
        if (personResponse != null)
            personAbstract.deletePerson(personResponse.getId());
        passportRepository.getPassportsByParams()
                .forEach(passport -> passportRepository.deletePassport(passport.getId()));
    }

    @Test
    void testFindAllPassportsWithOutParams() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract
                .findAllPassports(null, null, null)
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithActiveTrue() {
        var response = searchAbstract.findAllPassports(true, null, null)
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithActiveFalse() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassports(false, null, null)
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(new ArrayList<>(), response);
    }

    @Test
    void testFindAllPassportsWithDates() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassports(null,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-05-01T19:00:00+09:00")),
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-07-01T19:00:00+10:00")))
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithBadStringFormatDatesStartDate() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassportsWithString(null,
                        "2022-05-00+09:00",
                        "2022-07-01T19:00:00+10:00")
                .statusCode(400)
                .extract()
                .response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));
    }

    @Test
    void testFindAllPassportsWithBadStringFormatDatesEndDate() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassportsWithString(null,
                        "2022-05-01T19:00:00+09:00",
                        "________")
                .statusCode(400)
                .extract()
                .response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));
    }

    @Test
    void testFindAllPassportsWithActiveAndDates() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassports(true,
                        Instant.parse("2022-05-01T19:00:00+09:00"),
                        Instant.parse("2022-07-01T19:00:00+10:00"))
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithActiveAndBadDates() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassports(true,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-10-01T19:00:00+09:00")),
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-07-01T19:00:00+10:00")))
                .statusCode(400)
                .extract()
                .response().as(ErrorModel.class);
        assertEquals(env.getProperty("exception.InvalidPassportDataException"), response.getMessage());
    }

    @Test
    void testFindAllPassportsWithActiveAndBadDatesAndFalse() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassports(true,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-10-01T19:00:00+09:00")),
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-07-01T19:00:00+10:00")))
                .statusCode(400)
                .extract()
                .response().as(ErrorModel.class);
        assertEquals(env.getProperty("exception.InvalidPassportDataException"), response.getMessage());
    }

    @Test
    void testFindAllPassportsWithActiveAndStartDate() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassports(true,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-05-01T19:00:00+09:00")),
                        null)
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithActiveAndEndDate() {
        var number1 = new Number();
        number1.setNumber(String.valueOf(number));
        var response = searchAbstract.findAllPassports(true,
                        null,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-07-01T19:00:00+10:00")))
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(new ArrayList<>(), response);
    }
}
