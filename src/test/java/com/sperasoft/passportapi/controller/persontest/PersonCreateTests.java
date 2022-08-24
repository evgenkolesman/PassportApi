package com.sperasoft.passportapi.controller.persontest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.TestAbstractIntegration;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import io.restassured.RestAssured;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PersonCreateTests extends TestAbstractIntegration {

    public static final String INVALID_DATA_NAME_SIZE = "Invalid data: Name must be minimum 2 characters long";
    public static final String INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE =
            "Invalid data: Birthday country should be formatted like ISO CODE (2 characters)";
    public static final String INVALID_DATA_GIVEN_DATE_EMPTY = "Invalid data: Given Date field shouldn`t be empty";
    public static final String INVALID_DATA_BIRTHDAY_NOT_FILLED = "Invalid data: BirthdayCountry field should be filled";
    public static final String INVALID_DATA_NAME_NOT_FILLED = "Invalid data: Name field should be filled";

    @Autowired
    private Environment env;

    @LocalServerPort
    private int port;

    @Autowired
    private PersonTestMethodContainer personTestMethodContainer;

    @Autowired
    private UriComponentsBuilder builder;

    @Autowired
    PersonRepository personRepository;

    private PersonRequest personRequest;
    private PersonResponse personResponse;

    @BeforeEach
    void testDataProduce() {
        builder.port(port);
        RestAssured.port = port;
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        int intVar = ThreadLocalRandom.current().nextInt(10000);
        personRequest = new PersonRequest("Alex Frolov" + intVar, date, "UK");
    }

    @AfterEach
    public void testDataClear() {
        List<Person> allPersons = personRepository.findAll();
        if (allPersons.size() > 0)
            allPersons.forEach(per -> personRepository.deletePerson(per.getId()));
    }

    @Test
    void createCorrectPerson() {
        personResponse = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(200).extract().as(PersonResponse.class);
        assertEquals(personRequest.getName(), personResponse.getName());
        assertEquals(personRequest.getBirthday(), personResponse.getBirthday());
        assertEquals(personRequest.getBirthdayCountry(), personResponse.getBirthdayCountry());
    }

    @Test
    void createDoubleCorrectPerson() {
        personResponse = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(200).extract().as(PersonResponse.class);
        var response = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(400).extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.InvalidPersonDataException"))));
    }

    @Test
    void createNotCorrectPersonWithBadName() throws JsonProcessingException, JSONException {
        var response = personTestMethodContainer.createPerson("1",
                        "2000-10-11",
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();

        assertTrue(response.contains(INVALID_DATA_NAME_SIZE));
    }

    @Test
    void createNotCorrectPersonWithNoName() throws JsonProcessingException, JSONException {
        var response = personTestMethodContainer.createPerson("",
                        "2000-10-11",
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();

        assertTrue(response.contains(INVALID_DATA_NAME_SIZE));

    }

    @Test
    void createNotCorrectPersonWithWhitespaceName() throws JsonProcessingException, JSONException {
        var response = personTestMethodContainer.createPerson("_",
                        "2000-10-11",
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();

        assertTrue(response.contains(INVALID_DATA_NAME_SIZE));
    }

    @Test
    void createNotCorrectPersonWithNullName() throws JsonProcessingException, JSONException {
        var response = personTestMethodContainer.createPerson(null,
                        "2000-10-11",
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();
        assertTrue(response.contains(INVALID_DATA_NAME_NOT_FILLED));
    }

    @Test
    void createNotCorrectPersonWithBadCountryMoreThanTwo() throws JsonProcessingException, JSONException {
        String response = personTestMethodContainer.createPerson("1efefs dsfdsf",
                        "2000-10-11",
                        "RUS")
                .assertThat().statusCode(400)
                .and().extract().response().print();

        assertTrue(response.contains(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE));

    }

    @Test
    void createNotCorrectPersonWithBadCountryLessThanTwo() throws JsonProcessingException, JSONException {
        String response = personTestMethodContainer.createPerson("1efefs dsfdsf",
                        "2000-10-11",
                        "R")
                .assertThat().statusCode(400)
                .and().extract().response().print();

        assertTrue(response.contains(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE));

    }

    @Test
    void createNotCorrectPersonWithBadCountryNull() throws JsonProcessingException, JSONException {
        String response = personTestMethodContainer.createPerson("1efefs dsfdsf",
                        "2000-10-11",
                        null)
                .assertThat().statusCode(400)
                .and().extract().response().print();

        assertTrue(response.contains(INVALID_DATA_BIRTHDAY_NOT_FILLED));

    }

    @Test
    void createNotCorrectPersonWithBadDateNull() throws JsonProcessingException, JSONException {
        String response = personTestMethodContainer.createPerson("1efefs dsfdsf",
                        null,
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();

        assertTrue(response.contains(INVALID_DATA_GIVEN_DATE_EMPTY));

    }

    @Test
    void createNotCorrectPersonWithBadDateNotValid() throws JsonProcessingException, JSONException {
        var response = personTestMethodContainer.createPerson("1efefs dsfdsf",
                        "2000-10-111",
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));

    }

    @Test
    void createNotCorrectPersonWithBadDateNotValidFullString() throws JsonProcessingException, JSONException {
        var response = personTestMethodContainer.createPerson("1efefs dsfdsf",
                        "200010111",
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();
        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));

    }

}
