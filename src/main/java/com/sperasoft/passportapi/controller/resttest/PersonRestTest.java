package com.sperasoft.passportapi.controller.resttest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
public class PersonRestTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private PersonRequest personRequest;
    private Person person;
    private PersonResponse personResponse;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate dateToday = LocalDate.now();
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(dateToday);
        passport.setDepartmentCode("123123");
        personRequest = new PersonRequest();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = Person.of(personRequest);
        personResponse = PersonResponse.of(person);
    }

    @Test
    void testCreatePersonCorrect() throws JsonProcessingException {
        String req = mapper.writeValueAsString(personRequest);
        var response= given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req).
        when().put("http://localhost:8081/person")
                .then()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        assertEquals(response, personResponse);
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString("Alex Frolov")));
    }


}
