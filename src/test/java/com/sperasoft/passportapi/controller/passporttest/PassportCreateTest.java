package com.sperasoft.passportapi.controller.passporttest;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.abstracts.PassportTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.TestAbstractIntegration;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.repository.PassportRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PassportCreateTest extends TestAbstractIntegration {

    private static final String PASSPORT_NUMBER_NOT_FILLED = "Passport number field should be filled";
    private static final String PASSPORT_NUMBER_BAD_LENGTH = "Invalid data: Passport number should be 10 symbols length";
    private static final String PASSPORT_GIVEN_DATE_EMPTY = "Given Date field shouldn`t be empty";
    private static final String PASSPORT_DEPARTMENT_CODE_NOT_FILLED = "Invalid data: Department code field should be filled";
    private static final String PASSPORT_DEPARTMENT_CODE_NOT_DIGIT = "Invalid data: Invalid department code";
    private static final String PASSPORT_DEPARTMENT_CODE_BAD_SIZE = "Invalid data: department code size should be 6 digits";

    @Autowired
    private Environment env;

    @LocalServerPort
    private int port;

    @Autowired
    private PersonTestMethodContainer personTestMethodContainer;

    @Autowired
    private PassportTestMethodContainer passportTestMethodContainer;

    @Autowired
    private UriComponentsBuilder builder;
    @Autowired
    private PassportRepository passportRepository;
    private PassportRequest passportRequest;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;


    @BeforeEach
    void testDataProduce() {
        builder.port(port);
        RestAssured.port = port;
        int number = ThreadLocalRandom.current().nextInt(999999999) + 1000000000;
        int departmentCode = ThreadLocalRandom.current().nextInt(99999) + 100000;
        int varInt = ThreadLocalRandom.current().nextInt(10000000);
        passportRequest = new PassportRequest(String.valueOf(number),
                ZonedDateTime.of(LocalDate.of(2022, 5, 1),
                        LocalTime.of(20, 20, 20), ZoneId.systemDefault()).toInstant(),
                String.valueOf(departmentCode));
        PersonRequest personRequest = new PersonRequest("Alex Frolov" + varInt,
                LocalDate.now().minusYears(18),
                "UK");
        personResponse = personTestMethodContainer.createPerson(personRequest).extract().as(PersonResponse.class);
    }

    @AfterEach
    void testDataClear() {
        if (personResponse != null)
            personTestMethodContainer.deletePerson(personResponse.getId());

        passportRepository.getPassportsByParams()
                .forEach(passport -> passportRepository.deletePassport(passport.getId()));

    }

    @Test
    void createPassportWithCorrectData() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        assertEquals(passportResponse.getNumber(), passportRequest.getNumber());
        assertEquals(passportResponse.getDepartmentCode(), passportRequest.getDepartmentCode());
        assertEquals(passportResponse.getGivenDate(), passportRequest.getGivenDate().truncatedTo(ChronoUnit.MICROS));
    }

    @Test
    void createPassportWithCorrectDataDoubleTimes() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(400)
                .extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.PassportWasAddedException"))));
    }

    @Test
    void createPassportWithCorrectDataNotCorrectPerson() throws JsonProcessingException {
        String friendlyId = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.createPassport(friendlyId,
                        passportRequest)
                .assertThat().statusCode(404)
                .extract().response().print();
        assertTrue(response.contains(String.format(
                Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), friendlyId)));
    }

    @Test
    void createPassportWithNotCorrectDataBadNumberLong() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        "12343534564363546",
                        Instant.now().toString(),
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_BAD_LENGTH));
    }

    @Test
    void createPassportWithNotCorrectDataBadNumberShort() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        "363546",
                        Instant.now().toString(),
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_BAD_LENGTH));
    }

    @Test
    void createPassportWithNotCorrectDataBadNumberEmpty() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        "",
                        Instant.now().toString(),
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_BAD_LENGTH));
    }

    @Test
    void createPassportWithNotCorrectDataBadDepartmentCodeWithOneSymbol() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        "_",
                        Instant.now().toString(),
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_BAD_LENGTH));
    }

    @Test
    void createPassportWithNotCorrectDataBadDepartmentCodeNull() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        null,
                        Instant.now().toString(),
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_NOT_FILLED));
    }

    @Test
    void createPassportWithNotCorrectDataGivenDateNotDateTimeString() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        "Instant.now().toString()",
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));
    }

    @Test
    void createPassportWithNotCorrectDataGivenDateNotDateTimeDigits() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        "___23213 - 321",
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));
    }

    @Test
    void createPassportWithNotCorrectDataGivenDateNull() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        null,
                        "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_GIVEN_DATE_EMPTY));
    }

    //TODO SolveProblems with deserialization maybe I will understand how to solve it on Java 11

    @Test
    void createPassportWithCorrectDataGivenDateDateTimeDigits() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        "2022-03-05T19:00:00-02:00",
                        "123123")
                .assertThat().statusCode(200).extract().body().as(PassportResponse.class);
        assertEquals(response.getGivenDate(), Instant.parse("2022-03-05T19:00:00-02:00"));
    }

    @Test
    void createPassportWithNotCorrectDepartmentCodeShort() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        Instant.now().toString(),
                        "123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE));
    }

    @Test
    void createPassportWithNotCorrectDepartmentCodeLong() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        Instant.now().toString(),
                        "123213323123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE)
                || response.contains(PASSPORT_DEPARTMENT_CODE_NOT_DIGIT));
    }

    @Test
    void createPassportWithNotCorrectDepartmentCodeEmpty() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        Instant.now().toString(),
                        "")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_NOT_DIGIT)
                || response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE));
    }

    @Test
    void createPassportWithNotCorrectDepartmentCodeNotDigits() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        Instant.now().toString(),
                        "%^789")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_NOT_DIGIT)
                || response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE));
    }

    @Test
    void createPassportWithNotCorrectDepartmentCodeNull() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        Instant.now().toString(),
                        null)
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_NOT_FILLED));
    }

}
