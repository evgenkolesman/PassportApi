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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PassportFindPersonPassportTest extends TestAbstractIntegration {

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
    private final DateTimeFormatter isoOffsetDateTime = DateTimeFormatter.ISO_DATE_TIME;

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
    void testFindPersonPassportWithoutParamsCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                passportRequest).extract().as(PassportResponse.class);
        assertEquals(List.of(passportResponse),
                passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                                null, null, null)
                        .assertThat()
                        .statusCode(200)
                        .extract()
                        .body()
                        .jsonPath()
                        .getList("", PassportResponse.class));
    }

    @Test
    void testFindPersonPassportWithoutParamsCorrectBadPersonId() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                passportRequest).extract().as(PassportResponse.class);
        String personBadId = FriendlyId.createFriendlyId();
        var response =
                passportTestMethodContainer.findPersonPassports(personBadId,
                                null, null, null)
                        .assertThat()
                        .statusCode(404)
                        .extract()
                        .response().print();
        assertTrue(response.contains(
                String.format(Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), personBadId)));

    }

    @Test
    void testFindPersonPassportWithAllParamsCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                passportRequest).extract().as(PassportResponse.class);
        assertEquals(List.of(passportResponse),
                passportTestMethodContainer.findPersonPassports(
                                personResponse.getId(),
                                true,
                                ZonedDateTime.now().minusYears(1).toInstant(),
                                Instant.now())
                        .assertThat()
                        .statusCode(200)
                        .extract()
                        .body()
                        .jsonPath()
                        .getList("", PassportResponse.class));
    }

    @Test
    void testFindPersonPassportWithAllParamsCorrectBadPersonId() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                passportRequest).extract().as(PassportResponse.class);
        String personBadId = FriendlyId.createFriendlyId();
        var response =
                passportTestMethodContainer.findPersonPassports(
                                personBadId,
                                true,
                                ZonedDateTime.now().minusYears(1).toInstant(),
                                Instant.now())
                        .assertThat()
                        .statusCode(404)
                        .extract()
                        .response().print();
        assertTrue(response.contains(
                String.format(Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), personBadId)));


    }

    @Test
    void testFindPersonPassportWithActiveTrueCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                passportRequest).extract().as(PassportResponse.class);
        var passportResponseAnswer = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        true, null, null)
                .assertThat()
                .statusCode(200)
                .extract().body()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), passportResponseAnswer);
    }

    @Test
    void testFindPersonPassportWithActiveTrueCorrectWithBadPersonId() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                passportRequest).extract().as(PassportResponse.class);
        String personBadId = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.findPersonPassports(personBadId,
                        true, null, null)
                .assertThat()
                .statusCode(404)
                .extract().response().print();

        assertTrue(response.contains(String.format(
                Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), personBadId)));
    }

    @Test
    void testFindPersonPassportWithActiveFalseEmptyCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                passportRequest).extract().as(PassportResponse.class);
        var passportResponseAnswer = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        false, null, null).assertThat()
                .statusCode(200).extract().body()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(new ArrayList<>(), passportResponseAnswer);
    }

    @Test
    void testFindPersonPassportsWithDatesCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        null,
                        ZonedDateTime.now().minusYears(1).toInstant(),
                        Instant.now())
                .assertThat().statusCode(200)
                .extract()
                .body().jsonPath().getList("", PassportResponse.class);

        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindPersonPassportsWithDatesCorrectPersonIdNotCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        String personBadId = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.findPersonPassports(personBadId,
                        null,
                        ZonedDateTime.now().minusYears(1).toInstant(),
                        Instant.now())
                .assertThat().statusCode(404)
                .extract()
                .response().print();

        assertTrue(response.contains(String.format(
                Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), personBadId)));
    }

    @Test
    void testFindPersonPassportsWithDatesNotCorrect() {
        String id = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.findPersonPassports(id,
                        null,
                        Instant.from(isoOffsetDateTime.parse("2022-05-10T19:00:00-02:00")),
                        Instant.from(isoOffsetDateTime.parse("2022-05-08T19:00:00-02:00")))
                .assertThat().statusCode(400)
                .extract()
                .response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.InvalidPassportDataException"))));
    }

    @Test
    void testFindPersonPassportsWithStartDate() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        null,
                        Instant.from(isoOffsetDateTime.parse("2022-05-01T01:00:00-02:00")),
                        null)
                .assertThat().statusCode(200)
                .extract()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindPersonPassportsWithEndDate() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        null,
                        null,
                        ZonedDateTime.of(LocalDate.of(2022, 5, 1),
                                LocalTime.of(20, 30, 30), ZoneId.systemDefault()).toInstant())
                .assertThat().statusCode(200)
                .extract()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindPersonPassportsWithStartDateWithTrue() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        true,
                        Instant.from(isoOffsetDateTime.parse("2022-05-01T01:00:00-02:00")),
                        null)
                .assertThat().statusCode(200)
                .extract()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindPersonPassportsWithEndDateWithTrue() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        true,
                        null,
                        ZonedDateTime.of(LocalDate.of(2022, 5, 1),
                                LocalTime.of(20, 30, 30), ZoneId.systemDefault()).toInstant())
                .assertThat().statusCode(200)
                .extract()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindPersonPassportsWithStartDateWithFalse() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        false,
                        Instant.from(isoOffsetDateTime.parse("2022-05-01T01:00:00-02:00")),
                        null)
                .assertThat().statusCode(200)
                .extract()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(new ArrayList<>(), response);
    }

    @Test
    void testFindPersonPassportsWithEndDateWithFalse() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPersonPassports(personResponse.getId(),
                        false,
                        null,
                        ZonedDateTime.of(LocalDate.of(2022, 5, 1),
                                LocalTime.of(20, 30, 30), ZoneId.systemDefault()).toInstant())
                .assertThat().statusCode(200)
                .extract()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(new ArrayList<>(), response);
    }

}
