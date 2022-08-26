package com.sperasoft.passportapi.controller.passporttest;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.abstracts.PassportTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.TestAbstractIntegration;
import com.sperasoft.passportapi.controller.dto.*;
import com.sperasoft.passportapi.repository.PassportRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PassportDeleteTest extends TestAbstractIntegration {

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
    void deletePassportTestCorrectId() throws JsonProcessingException {
        PassportResponse passportResponse = passportTestMethodContainer.createPassport(personResponse.getId(), passportRequest)
                .extract().as(PassportResponse.class);
        passportTestMethodContainer.deletePassport(personResponse.getId(), passportResponse.getId())
                .assertThat().statusCode(204);
    }

    @Test
    void deletePassportTestNotCorrectBadId() {
        String friendlyId = FriendlyId.createFriendlyId();
        var response = passportTestMethodContainer.deletePassport(personResponse.getId(), friendlyId)
                .assertThat().statusCode(404).extract().response().as(TestErrorModel.class);
        assertThat(response.getMessage()).isEqualTo(String.format(
                Objects.requireNonNull(env.getProperty("exception.PassportNotFoundException")), friendlyId));
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
