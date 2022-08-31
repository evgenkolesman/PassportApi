package com.sperasoft.passportapi.controller.searchtest;

import com.sperasoft.passportapi.controller.abstracts.PassportTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.SearchTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.TestAbstractIntegration;
import com.sperasoft.passportapi.controller.dto.*;
import com.sperasoft.passportapi.model.ErrorModel;
import com.sperasoft.passportapi.model.Number;
import com.sperasoft.passportapi.repository.PassportRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchPassportByNumberTest extends TestAbstractIntegration {


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
        passportAbstract.createPassport(personResponse.getId(), passportRequest)
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
    void testFindPersonByPassportNumberCorrect() throws Exception {
        var number1 = new Number(String.valueOf(number));
        var response = searchAbstract.findPersonByPassportNumber(number1)
                .statusCode(200)
                .extract()
                .body().as(PersonResponse.class);
        assertEquals(personResponse, response);
    }

    @Test
    void testFindPersonByPassportNumberNotCorrectLengthNumber() throws Exception {
        var number1 = new Number(
                String.valueOf(ThreadLocalRandom.current().nextInt(10000000)));
        var response = searchAbstract.findPersonByPassportNumber(number1)
                .statusCode(400)
                .extract()
                .response().as(ErrorModel.class);
        assertEquals(env.getProperty("exception.PassportWrongNumberException"), response.getMessage());
    }

    @Test
    void testFindPersonByPassportNumberNotCorrect() throws Exception {
        var number1 = new Number(String.valueOf(number - 100));
        var response = searchAbstract.findPersonByPassportNumber(number1)
                .statusCode(400)
                .extract()
                .response().as(ErrorModel.class);
        assertEquals(env.getProperty("exception.PassportWrongNumberException"), response.getMessage());
    }

    //TODO check this exception
    @Test
    void testFindPersonByPassportNumberNotCorrectNull() throws Exception {
        var response = searchAbstract.findPersonByPassportNumber(null)
                .statusCode(400)
                .extract()
                .response().as(TestErrorModel.class);
        assertThat(response.getMessage())
                .isEqualTo(Objects.requireNonNull(env.getProperty("exception.BadDateFormat")));
    }

}
