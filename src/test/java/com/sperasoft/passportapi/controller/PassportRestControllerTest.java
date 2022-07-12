package com.sperasoft.passportapi.controller;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.controller.rest.abstracts.PassportTestMethodContainer;
import com.sperasoft.passportapi.controller.rest.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.model.LostPassportInfo;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PassportRestControllerTest {

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

    private PassportRequest passportRequest;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private PersonRequest personRequest;
    private final DateTimeFormatter isoOffsetDateTime = DateTimeFormatter.ISO_DATE_TIME;

    @BeforeEach
    void testDataProduce() throws Exception {
        builder.port(port);
        RestAssured.port = port;
        int number = ThreadLocalRandom.current().nextInt(999999999) + 1000000000;
        int departmentCode = ThreadLocalRandom.current().nextInt(99999) + 100000;
        int varInt = ThreadLocalRandom.current().nextInt(10000000);
        passportRequest = new PassportRequest(String.valueOf(number),
                ZonedDateTime.of(LocalDate.of(2022, 5, 01),
                        LocalTime.of(20, 20, 20), ZoneId.systemDefault()).toInstant(),
                String.valueOf(departmentCode));
        personRequest = new PersonRequest("Alex Frolov" + varInt,
                LocalDate.now().minusYears(18),
                "UK");
        personResponse = personTestMethodContainer.createPerson(personRequest).extract().as(PersonResponse.class);
    }

    @AfterEach
    void testDataClear() {
        personTestMethodContainer.deletePerson(personResponse.getId());
        try {
            passportTestMethodContainer.deletePassport(personResponse.getId(), passportResponse.getId());
        } catch (Exception e) {
            log.info("passport was already removed");
        }
        //TODO need to make universal way to clear test data may be that way
    }

    @Test
    void createPassportWithCorrectData() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        assertEquals(passportResponse.getNumber(), passportRequest.getNumber());
        assertEquals(passportResponse.getDepartmentCode(), passportRequest.getDepartmentCode());
        assertEquals(passportResponse.getGivenDate(), passportRequest.getGivenDate());
    }

    @Test
    void createPassportWithNotCorrectDataBadNumber() throws JsonProcessingException {
        passportTestMethodContainer.createPassport(personResponse.getId(),
                        "12343534564363546",
                        Instant.now(),
                        "123123")
                .assertThat().statusCode(400);
    }

    @Test
    void createPassportWithNotCorrectDataBadDepartmentCode() throws JsonProcessingException {
        passportTestMethodContainer.createPassport(personResponse.getId(),
                        "1234675678",
                        Instant.now(),
                        "123")
                .assertThat().statusCode(400);
    }

    @Test
    void updatePassportWithCorrectDataNumber() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        String number = "1111111111";
        PassportResponse passportResponseUpdate =
                passportTestMethodContainer.updatePassport(personResponse.getId(),
                                passportResponse.getId(),
                                number,
                                passportRequest.getGivenDate(),
                                passportRequest.getDepartmentCode())
                        .assertThat().statusCode(200)
                        .extract().as(PassportResponse.class);
        assertEquals(passportResponseUpdate.getNumber(), number);

    }

    @Test
    void updatePassportWithNotCorrectDataBadNumber() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "123", Instant.now(), "123123")
                .assertThat().statusCode(400);
    }

    @Test
    void updatePassportWithNotCorrectDataBadDepartmentCode() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "1234675678", Instant.now(), "123")
                .assertThat().statusCode(400);
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
    void testFindPersonPassportsWithDatesNotCorrect() {
        String id = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.findPersonPassports(id,
                        null,
                        Instant.from(isoOffsetDateTime.parse("2022-05-10T19:00:00-02:00")),
                        Instant.from(isoOffsetDateTime.parse("2022-05-08T19:00:00-02:00")))
                .assertThat().statusCode(400)
                .extract()
                .response().print();
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
                        ZonedDateTime.of(LocalDate.of(2022, 5, 01),
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
                        ZonedDateTime.of(LocalDate.of(2022, 5, 01),
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
                        ZonedDateTime.of(LocalDate.of(2022, 5, 01),
                                LocalTime.of(20, 30, 30), ZoneId.systemDefault()).toInstant())
                .assertThat().statusCode(200)
                .extract()
                .jsonPath().getList("", PassportResponse.class);
        assertEquals(new ArrayList<>(), response);
    }

    @Test
    void testFindPassportNotCorrectId() {
        String id = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.findPassport(personResponse.getId(), id, null)
                .assertThat().statusCode(404)
                .extract()
                .response().print();
        assertEquals(String.format(env.getProperty("exception.PassportNotFoundException"), id), response);
    }


    @Test
    void testFindPassportWithParamsCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest).extract().as(PassportResponse.class);
        PassportResponse testPassportResponse = passportTestMethodContainer.findPassport(personResponse.getId(), passportResponse.getId(), true)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        assertEquals(passportResponse, testPassportResponse);
    }


    @Test
    void testLostPassportCorrectWithDescription() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.lostPassportDeactivate(personResponse.getId(),
                        passportResponse.getId(),
                        new LostPassportInfo("I lost my passport"))
                .assertThat().statusCode(200)
                .extract()
                .response().equals("true");
    }

    @Test
    void testLostPassportCorrectWithEmptyDescription() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.lostPassportDeactivate(personResponse.getId(),
                        passportResponse.getId(),
                        null)
                .assertThat().statusCode(200)
                .extract()
                .response().equals("true");
    }

    @Test
    void testLostPassportNotCorrect() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.lostPassportDeactivate(personResponse.getId(), passportResponse.getId(), null)
                .assertThat().statusCode(200);
        var response = passportTestMethodContainer
                .lostPassportDeactivate(personResponse.getId(), passportResponse.getId(), null)
                .assertThat().statusCode(409)
                .extract()
                .response().print();
        assertEquals(env.getProperty("exception.PassportDeactivatedException"), response);
    }

    @Test
    void deletePassportTestCorrectId() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.deletePassport(personResponse.getId(), passportResponse.getId())
                .assertThat().statusCode(204);
    }

    @Test
    void deletePassportTestNotCorrectBadId() {
        passportTestMethodContainer.deletePassport(personResponse.getId(), FriendlyId.createFriendlyId())
                .assertThat().statusCode(404);
    }
}
