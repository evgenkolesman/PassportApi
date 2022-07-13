package com.sperasoft.passportapi.controller;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.controller.rest.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonRestControllerTest {

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
        try {
            List<Person> all = personRepository.findAll();
            for (Person person : all) {
                personTestMethodContainer.deletePerson(person.getId());
            }
        } catch (PersonNotFoundException e) {
            log.info("Person was removed");
        }
    }


    @Test
    void createCorrectPerson() throws JsonProcessingException {
        personTestMethodContainer.createPerson(personRequest).assertThat().statusCode(200);
    }

    @Test
    void createNotCorrectPersonWithBadName() throws JsonProcessingException, JSONException {
        String response = personTestMethodContainer.createPerson("1",
                        LocalDate.of(2000, 10, 11),
                        "RU")
                .assertThat().statusCode(400)
                .and().extract().response().print();

//        assertEquals(env.getProperty("exception.InvalidPersonDataException"), response);
    }

    @Test
    void createNotCorrectPersonWithBadCountry() throws JsonProcessingException, JSONException {
        String response = personTestMethodContainer.createPerson("1efefs dsfdsf",
                        LocalDate.of(2000, 10, 11),
                        "RUS")
                .assertThat().statusCode(400)
                .and().extract().response().print();

//        assertEquals(env.getProperty("exception.InvalidPersonDataException"), response);
    }

    @Test
    void testFindPersonById() throws JsonProcessingException {
        PersonResponse personResponseForTest = personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        var response = personTestMethodContainer.findPersonById(personResponseForTest.getId())
                .assertThat().statusCode(200)
                .and().log()
                .all()
                .extract().response()
                .body().as(PersonResponse.class);
        assertEquals(response, personResponseForTest);
    }

    @Test
    void testFindPersonByIdNotCorrect() throws JsonProcessingException {
        String id = FriendlyId.createFriendlyId();
        personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        var response = personTestMethodContainer.findPersonById(id)
                .assertThat().statusCode(404)
                .and().log()
                .all()
                .extract().response()
                .body().as(ErrorModel.class);
        assertEquals(String.format(env.getProperty("exception.PersonNotFoundException"), id), response.getMessage());

    }

    @Test
    void testUpdatePersonByIdCorrectName() throws Exception {
        PersonRequest personRequest1 = new PersonRequest("Egor",
                personRequest.getBirthday(),
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
    void testUpdatePersonByIdCorrectNameAndBirthday() throws Exception {
        PersonRequest personRequest1 = new PersonRequest("Egor",
                LocalDate.of(2001, 12, 12),
                personRequest.getBirthdayCountry());
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract().as(PersonResponse.class);
        PersonResponse personResponse = personTestMethodContainer.updatePerson(personResponseForTest.getId(), personRequest1)
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        assertEquals(personRequest1.getName(), personResponse.getName());
    }

    //TODO FIX @Size
    @Test
    void testUpdatePersonByIdNameNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract().as(PersonResponse.class);
        var errorMessage = personTestMethodContainer.updatePerson(personResponseForTest.getId(), "1",
                        LocalDate.of(2001, 12, 12),
                        personRequest.getBirthdayCountry())
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();
//        assertEquals(String.format(env.getProperty("exception.InvalidPersonDataException")), errorMessage);
    }

    //TODO fix mistakes
    @Test
    void testUpdatePersonByIdBirthdayCountryNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var errorMessage = personTestMethodContainer.updatePerson(personResponseForTest.getId(), "Name Name",
                        LocalDate.of(2001, 12, 12),
                        "China")
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();
//        assertEquals(String.format(env.getProperty("exception.InvalidPersonDataException")), errorMessage);
    }

    @Test
    void testUpdatePersonByIdEmptyNameNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var errorMessage = personTestMethodContainer.updatePerson(personResponseForTest.getId(), "",
                        LocalDate.of(2001, 12, 12),
                        "CH")
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();
//        assertEquals(String.format(env.getProperty("exception.InvalidPersonDataException")), errorMessage);
    }

    @Test
    void testUpdatePersonByIdEmptyBirthdayCountryNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var errorMessage = personTestMethodContainer.updatePerson(personResponseForTest.getId(), "sadad sdad",
                        LocalDate.of(2001, 12, 12),
                        "")
                .assertThat().statusCode(400)
                .extract().response()
                .body().print();
//        assertEquals(String.format(env.getProperty("exception.InvalidPersonDataException")), errorMessage);
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
        assertEquals(String.format(env.getProperty("exception.PersonNotFoundException"), wrongId),
                errorMessage.getMessage());

    }

    @Test
    void deletePersonCorrect() throws JsonProcessingException {
        var personResponse = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(200).extract().as(PersonResponse.class);
        personTestMethodContainer.deletePerson(personResponse.getId()).assertThat().statusCode(204);
    }

    @Test
    void deletePersonNotCorrectNoPerson() throws JsonProcessingException {
        var personResponse = personTestMethodContainer.createPerson(personRequest)
                .assertThat().statusCode(200).extract().as(PersonResponse.class);
        String id = personResponse.getId();
        personTestMethodContainer.deletePerson(id).assertThat().statusCode(204);
        var errorMessage = personTestMethodContainer.deletePerson(id).assertThat()
                .statusCode(404).assertThat()
                .extract()
                .response()
                .body().as(ErrorModel.class);
        assertEquals(String.format(env.getProperty("exception.PersonNotFoundException"),
                        id),
                errorMessage.getMessage());
    }

}
