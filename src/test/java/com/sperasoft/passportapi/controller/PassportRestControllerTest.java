package com.sperasoft.passportapi.controller;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.controller.rest.abstracts.PassportTestMethodContainer;
import com.sperasoft.passportapi.controller.rest.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.model.ErrorModel;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PassportRestControllerTest {

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

    private PassportRequest passportRequest;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private PersonRequest personRequest;
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

    /** Create Passport tests
     *
     */

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
        || response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE) );
    }

    @Test
    void createPassportWithNotCorrectDepartmentCodeNotDigits() throws JsonProcessingException {
        var response = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest.getNumber(),
                        Instant.now().toString(),
                        "%^789")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_NOT_DIGIT)
                || response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE) );
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

    /** Update Passport tests
     *
     */

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
                                passportRequest.getGivenDate().toString(),
                                passportRequest.getDepartmentCode())
                        .assertThat().statusCode(200)
                        .extract().as(PassportResponse.class);
        assertEquals(passportResponseUpdate.getNumber(), number);

    }

    @Test
    void updatePassportWithNotCorrectDataBadIdPassport() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        String number = "1111111111";
        String friendlyId = FriendlyId.createFriendlyId();
        var response =
                passportTestMethodContainer.updatePassport(personResponse.getId(),
                                 friendlyId,
                                number,
                                passportRequest.getGivenDate().toString(),
                                passportRequest.getDepartmentCode())
                        .assertThat().statusCode(404)
                        .extract().response().print();
        assertTrue(response.contains(String.format(Objects.requireNonNull(
                env.getProperty("exception.PassportNotFoundException")), friendlyId)));

    }

    @Test
    void updatePassportWithNotCorrectDataBadNumberShort() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "123", Instant.now().toString(), "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_BAD_LENGTH));
    }


    @Test
    void updatePassportWithNotCorrectDataBadNumberLong() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "12321334243543", Instant.now().toString(), "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_BAD_LENGTH));
    }

    @Test
    void updatePassportWithNotCorrectDataBadNumberEmpty() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "", Instant.now().toString(), "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_BAD_LENGTH));
    }

    @Test
    void updatePassportWithNotCorrectDataBadNumberNull() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        null, Instant.now().toString(), "123123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_NUMBER_NOT_FILLED));
    }

    @Test
    void updatePassportWithNotCorrectDataBadDepartmentCodeShort() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "1234675678", Instant.now().toString(), "123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE));
    }


    @Test
    void updatePassportWithNotCorrectDataBadDepartmentCodeLong() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "1234675678", Instant.now().toString(), "123")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_BAD_SIZE));
    }


    @Test
    void updatePassportWithNotCorrectDataBadDepartmentCodeNotDigits() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "1234675678", Instant.now().toString(), "*as*$$")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_NOT_DIGIT));
    }

    @Test
    void updatePassportWithNotCorrectDataBadDepartmentCodeNull() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "1234675678", Instant.now().toString(), null)
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_DEPARTMENT_CODE_NOT_FILLED));
    }


    @Test
    void updatePassportWithNotCorrectDataBadDateAsString() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "1234675678", "dsa", "123456")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));
    }

    @Test
    void updatePassportWithNotCorrectDataBadDateAsNull() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.updatePassport(personResponse.getId(), passportResponse.getId(),
                        "1234675678", null, "123456")
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(PASSPORT_GIVEN_DATE_EMPTY));
    }


    /** FindPersonPassport Passport tests
     *
     */

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

    /** FindPassport Passport tests
     *
     */

    @Test
    void testFindPassportWithParamsCorrectActiveTrue() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest).extract().as(PassportResponse.class);
        PassportResponse testPassportResponse = passportTestMethodContainer.findPassport(personResponse.getId(), passportResponse.getId(), true)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);
        assertEquals(passportResponse, testPassportResponse);
    }

    @Test
    void testFindPassportNotCorrectId() {
        String id = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.findPassport(personResponse.getId(), id, null)
                .assertThat().statusCode(404)
                .extract()
                .response().as(ErrorModel.class);
        assertEquals(String.format(Objects.requireNonNull(env.getProperty("exception.PassportNotFoundException")),
                id), response.getMessage());
    }

//TODO fix this
    @Test
    void testFindPassportWithParamsCorrectActiveFalse() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest).extract().as(PassportResponse.class);
        var response = passportTestMethodContainer.findPassport(personResponse.getId(), passportResponse.getId(), false)
                .assertThat().statusCode(400)
                .extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.PassportBadStatusException"))));
    }

    /** LostPassport Passport tests
     *
     */

    @Test
    void testLostPassportCorrectWithDescription() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        assertEquals(true, passportTestMethodContainer.lostPassportDeactivate(personResponse.getId(),
                        passportResponse.getId(),
                        new LostPassportInfo("I lost my passport"))
                .assertThat().statusCode(200)
                .extract()
                .as(Boolean.class));
    }

    @Test
    void testLostPassportCorrectWithEmptyDescription() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        assertEquals(true, passportTestMethodContainer.lostPassportDeactivate(personResponse.getId(),
                        passportResponse.getId(),
                        null)
                .assertThat().statusCode(200)
                .extract()
                .response().as(Boolean.class));
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
                .response().as(ErrorModel.class);
        assertEquals(env.getProperty("exception.PassportDeactivatedException"), response.getMessage());
    }

    @Test
    void testLostPassportNotCorrectNoPassportId() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.lostPassportDeactivate(personResponse.getId(), passportResponse.getId(), null)
                .assertThat().statusCode(200);
        passportTestMethodContainer
                .lostPassportDeactivate(personResponse.getId(), null, null)
                .assertThat().statusCode(405);
    }


    /** Delete Passport tests
     *
     */

    @Test
    void deletePassportTestCorrectId() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.deletePassport(personResponse.getId(), passportResponse.getId())
                .assertThat().statusCode(204);
    }

    @Test
    void deletePassportTestNotCorrectBadId() {
        String friendlyId = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.deletePassport(personResponse.getId(), friendlyId)
                .assertThat().statusCode(404).extract().response().print();
        assertTrue(response.contains(String.format(
                Objects.requireNonNull(env.getProperty("exception.PassportNotFoundException")), friendlyId)));
    }

    @Test
    void deletePassportTestNotCorrectBadIdEmpty() {
        passportTestMethodContainer.deletePassport(personResponse.getId(), "")
                .assertThat().statusCode(405);
    }


    @Test
    void deletePassportTestNotCorrectBadIdNull() {
        passportTestMethodContainer.deletePassport(personResponse.getId(), null)
                .assertThat().statusCode(405);
    }
}
