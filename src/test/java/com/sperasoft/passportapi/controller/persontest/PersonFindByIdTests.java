package com.sperasoft.passportapi.controller.persontest;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.TestAbstractIntegration;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.ErrorModel;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import io.restassured.RestAssured;
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

public class PersonFindByIdTests extends TestAbstractIntegration {

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
        List<Person> allPersons = personRepository.findAll();
        if (allPersons.size() > 0)
            allPersons.forEach(per -> personRepository.deletePerson(per.getId()));
    }

    @Test
    void testFindPersonById() {
        PersonResponse personResponse = personTestMethodContainer.createPerson(personRequest)
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

}
