package com.sperasoft.passportapi.controller;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequestTest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.controller.rest.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.model.ErrorModel;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonRestControllerTest {

    public static final String INVALID_DATA_NAME_SIZE = "Invalid data: Name must be minimum 2 characters long";
    public static final String INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE =
            "Invalid data: Birthday country should be formatted like ISO CODE (2 characters)";
    public static final String INVALID_DATA_GIVEN_DATE_EMPTY = "Invalid data: Given Date field shouldn`t be empty";
    public static final String INVALID_DATA_BIRTHDAY_NOT_FILLED = "Invalid data: BirthdayCountry field should be filled";
    public static final String INVALID_DATA_NAME_NOT_FILLED = "Invalid data: Name field should be filled";

    private final Set<String> cacheDeletePersonId = new LinkedHashSet<>();

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

    /**
     * Creation Person tests
     */

    @Test
    void createCorrectPerson() {
        personResponse = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(200).extract().as(PersonResponse.class);
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

    /**
     * Update Person tests
     */

    @Test
    void testUpdatePersonByIdCorrectName() throws Exception {
        PersonRequestTest personRequest1 = new PersonRequestTest("Egor",
                personRequest.getBirthday().toString(),
                personRequest.getBirthdayCountry());
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract().as(PersonResponse.class);
        PersonResponse personResponse = personTestMethodContainer.updatePerson(personResponseForTest.getId(), personRequest1)
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        assertEquals(personRequest1.getName(), personResponse.getName());
    }

    @Test
    void testUpdatePersonByIdNameNotCorrectOneSymbol() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTest("#",
                                "2000-10-11",
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();

        assertTrue(response.contains(INVALID_DATA_NAME_SIZE));
    }

    @Test
    void testUpdatePersonByIdNameNotCorrectEmpty() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTest("",
                                "2000-10-11",
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();

        assertTrue(response.contains(INVALID_DATA_NAME_SIZE));
    }

    @Test
    void testUpdatePersonByIdBirthdayNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTest("Alex Alex",
                                "2000-1011",
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();

        assertTrue(response.contains(Objects.requireNonNull(env.getProperty("exception.BadDateFormat"))));
    }

    @Test
    void testUpdatePersonByIdBirthdayNotCorrectNull() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTest("Alex Alex",
                                null,
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();

        assertTrue(response.contains(INVALID_DATA_GIVEN_DATE_EMPTY));
    }

    @Test
    void testUpdatePersonByIdBirthdayCountryEmptyNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTest("Alex Alex",
                                "2000-10-11",
                                ""))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();

        assertTrue(response.contains(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE));
    }

    @Test
    void testUpdatePersonByIdBirthdayCountryOneSymbolNotCorrectEmpty() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTest("Alex Alex",
                                "2000-10-11",
                                "4"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();

        assertTrue(response.contains(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE));
    }


    @Test
    void testUpdatePersonByIdBirthdayCountryThreeSymbolNotCorrectEmpty() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTest("Alex Alex",
                                "2000-10-11",
                                "DSD"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();

        assertTrue(response.contains(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE));
    }


    @Test
    void testUpdatePersonByIdBirthdayCountryNullNotCorrect() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTest("Alex Alex",
                                "2000-10-11",
                                null))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();
        assertTrue(response.contains(INVALID_DATA_BIRTHDAY_NOT_FILLED));
    }

    @Test
    void testUpdatePersonByIdEmptyBirthdayCountryNotCorrect() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var errorMessage = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTest("Alex Alex",
                                "2000-10-11",
                                ""))
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();
        assertTrue(errorMessage.contains(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE));
    }

    @Test
    void testUpdatePersonByIdNotCorrectId() throws Exception {
        PersonRequest personRequest1 = new PersonRequest("Egor",
                personRequest.getBirthday(),
                personRequest.getBirthdayCountry());
        personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        var wrongId = FriendlyId.createFriendlyId();
        var errorMessage = personTestMethodContainer.updatePerson(wrongId, personRequest1)
                .assertThat().statusCode(404)
                .extract().response()
                .body().as(ErrorModel.class);
        assertEquals(String.format(Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), wrongId),
                errorMessage.getMessage());

    }

    /**
     * FindById Person tests
     */

    @Test
    void testFindPersonById() {
        personResponse = personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        var response = personTestMethodContainer.findPersonById(personResponse.getId())
                .assertThat().statusCode(200)
                .and().log()
                .all()
                .extract().response()
                .body().as(PersonResponse.class);
        assertEquals(response, personResponse);
    }

    @Test
    void testFindPersonByIdRandomNotCorrect() {
        String id = FriendlyId.createFriendlyId();
        personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        var response = personTestMethodContainer.findPersonById(id)
                .assertThat().statusCode(404)
                .and().log()
                .all()
                .extract().response()
                .body().as(ErrorModel.class);
        assertEquals(String.format(Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), id),
                response.getMessage());

    }

    @Test
    void testFindPersonByIdNullNotCorrect() {
        personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        personTestMethodContainer.findPersonById(null)
                .assertThat().statusCode(405);

    }

    @Test
    void testFindPersonByIdEmptyNotCorrect() {
        personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        personTestMethodContainer.findPersonById("")
                .assertThat().statusCode(405)
                .and().log()
                .all()
                .extract().response()
                .print();

    }

    /**
     * Delete Person tests
     */


    @Test
    void deletePersonCorrect() {
        personResponse = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(200).extract().as(PersonResponse.class);
        personTestMethodContainer.deletePerson(personResponse.getId()).assertThat().statusCode(204);
    }

    @Test
    void deletePersonNullIdNotCorrect() {
        personTestMethodContainer.deletePerson(null).assertThat().statusCode(405);
    }

    @Test
    void deletePersonNotCorrectNoPerson() {
        var personResponse = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(200).extract().as(PersonResponse.class);
        String id = personResponse.getId();
        personTestMethodContainer.deletePerson(id).assertThat().statusCode(204);
        var errorMessage = personTestMethodContainer.deletePerson(id).assertThat()
                .statusCode(404).assertThat()
                .extract()
                .response()
                .body().as(ErrorModel.class);
        assertEquals(String.format(Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")),
                        id),
                errorMessage.getMessage());
    }

}
