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
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PassportUpdateTest extends TestAbstractIntegration {

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
    private PersonRequest personRequest;

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
        if (personResponse != null)
            personTestMethodContainer.deletePerson(personResponse.getId());
        passportRepository.getPassportsByParams()
                .forEach(passport -> passportRepository.deletePassport(passport.getId()));

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
                                passportRequest.getGivenDate().toString(),
                                passportRequest.getDepartmentCode())
                        .assertThat().statusCode(200)
                        .extract().as(PassportResponse.class);
        assertEquals(passportResponseUpdate.getNumber(), number);

    }

    @Test
    void updatePassportWithCorrectDataNumberBadPersonId() throws JsonProcessingException {
        var badPersonId = FriendlyId.createFriendlyId();
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(),
                        passportRequest)
                .assertThat().statusCode(200)
                .extract().as(PassportResponse.class);

        var response =
                passportTestMethodContainer.updatePassport(badPersonId, passportResponse.getId(),
                                passportRequest)
                        .assertThat().statusCode(404)
                        .extract().response().print();
        assertTrue(response.contains(String.format(
                Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), badPersonId)));

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

}
