package com.sperasoft.passportapi.controller.persontest;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.abstracts.PersonTestMethodContainer;
import com.sperasoft.passportapi.controller.abstracts.TestAbstractIntegration;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequestTestModel;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.controller.dto.TestErrorModel;
import com.sperasoft.passportapi.model.ErrorModel;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.utils.UriComponentsBuilderUtil;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonUpdateTest extends TestAbstractIntegration {

    public static final String INVALID_DATA_NAME_SIZE = "Invalid data: Name must be minimum 2 characters long";
    public static final String INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE =
            "Invalid data: Birthday country should be formatted like ISO CODE (2 characters)";
    public static final String INVALID_DATA_GIVEN_DATE_EMPTY = "Invalid data: Given Date field shouldn`t be empty";
    public static final String INVALID_DATA_BIRTHDAY_NOT_FILLED = "Invalid data: BirthdayCountry field should be filled";

    @Autowired
    private Environment env;

    @LocalServerPort
    private int port;

    @Autowired
    private PersonTestMethodContainer personTestMethodContainer;

    @Autowired
    PersonRepository personRepository;

    private PersonRequest personRequest;
    private PersonResponse personResponse;

    @BeforeEach
    void testDataProduce() {
        UriComponentsBuilderUtil.builder().port(port);
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
    void testUpdatePersonByIdCorrectName() throws Exception {
        PersonRequestTestModel personRequest1 = new PersonRequestTestModel("Egor",
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
                        new PersonRequestTestModel("#",
                                "2000-10-11",
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);

        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_NAME_SIZE);
    }

    @Test
    void testUpdatePersonByIdNameNotCorrectEmpty() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTestModel("",
                                "2000-10-11",
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);

        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_NAME_SIZE);
    }

    @Test
    void testUpdatePersonByIdBirthdayNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTestModel("Alex Alex",
                                "2000-1011",
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);

        assertThat(response.getMessage())
                .isEqualTo(Objects.requireNonNull(env.getProperty("exception.BadDateFormat")));
    }

    @Test
    void testUpdatePersonByIdBirthdayNotCorrectNull() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTestModel("Alex Alex",
                                null,
                                "CH"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);

        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_GIVEN_DATE_EMPTY);
    }

    @Test
    void testUpdatePersonByIdBirthdayCountryEmptyNotCorrect() throws Exception {
        PersonResponse personResponseForTest =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponseForTest.getId(),
                        new PersonRequestTestModel("Alex Alex",
                                "2000-10-11",
                                ""))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);

        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE);
    }

    @Test
    void testUpdatePersonByIdBirthdayCountryOneSymbolNotCorrectEmpty() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTestModel("Alex Alex",
                                "2000-10-11",
                                "4"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);

        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE);
    }


    @Test
    void testUpdatePersonByIdBirthdayCountryThreeSymbolNotCorrectEmpty() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTestModel("Alex Alex",
                                "2000-10-11",
                                "DSD"))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);

        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE);
    }


    @Test
    void testUpdatePersonByIdBirthdayCountryNullNotCorrect() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTestModel("Alex Alex",
                                "2000-10-11",
                                null))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);
        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_BIRTHDAY_NOT_FILLED);
    }

    @Test
    void testUpdatePersonByIdEmptyBirthdayCountryNotCorrect() throws Exception {
        personResponse =
                personTestMethodContainer.createPerson(personRequest)
                        .extract()
                        .as(PersonResponse.class);
        var response = personTestMethodContainer.updatePerson(personResponse.getId(),
                        new PersonRequestTestModel("Alex Alex",
                                "2000-10-11",
                                ""))
                .assertThat().statusCode(400)
                .extract().response()
                .body().as(TestErrorModel.class);
        assertThat(response.getMessage())
                .isEqualTo(INVALID_DATA_BIRTHDAY_COUNTRY_ISO_CODE);
    }

    @Test
    void testUpdatePersonByIdNotCorrectId() {
        PersonRequest personRequest1 = new PersonRequest("Egor",
                personRequest.getBirthday(),
                personRequest.getBirthdayCountry());
        personTestMethodContainer.createPerson(personRequest)
                .extract().as(PersonResponse.class);
        var wrongId = FriendlyId.createFriendlyId();
        var response = personTestMethodContainer.updatePerson(wrongId, personRequest1)
                .assertThat().statusCode(404)
                .extract().response()
                .body().as(ErrorModel.class);
        assertThat(response.getMessage())
                .isEqualTo(Objects.requireNonNull(env.getProperty("exception.PersonNotFoundException")), wrongId);
    }
}
