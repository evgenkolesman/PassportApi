package com.sperasoft.passportapi.controller.passporttest;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.abstracts.PassportTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.ErrorModel;
import com.sperasoft.passportapi.model.LostPassportInfo;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PassportLostPassportTest {

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
    void testLostPassportCorrectWithDescriptionAndBadPersonId() throws JsonProcessingException {
        passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        String personBadId = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.lostPassportDeactivate(personBadId,
                        passportResponse.getId(),
                        new LostPassportInfo("I lost my passport"))
                .assertThat().statusCode(404)
                .extract()
                .response().print();
        assertTrue(response.contains(
                String.format(Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), personBadId)));

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
}
