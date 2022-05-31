package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@SpringBootTest(classes = PassportApiApplication.class)
public class SearchServiceTest {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PassportRepository passportRepository;
    @Autowired
    private PassportService passportService;
    @Autowired
    private SearchService searchService;

    @Autowired
    private PersonService personService;

    private Passport passport;
    private Person person;

    @BeforeEach
    private void testDataProduce() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        PassportRequest passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(LocalDate.now());
        passportRequest.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = personService.addPerson(Person.of(personRequest));
        passport = passportService.addPassportToPerson(person.getId(), Passport.of(passportRequest));
    }

    @AfterEach
    private void testDataClear() {
        passportRepository.deletePassport(passport.getId());
        personRepository.deletePerson(person.getId());
    }

    @Test
    public void findPersonByPassportNumberTest() {
        assertThat("Problems with adding person", searchService.findPersonByPassportNumber("1223123113")
                .equals(person));
    }

    @Test
    void testGetAllPassportsAllParams() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(true,
                        ZonedDateTime.parse("2022-03-05T19:00:00-02:00"), ZonedDateTime.now()));
    }

    @Test
    void testGetAllPassportsBadDate() {
        assertThrowsExactly(InvalidPassportDataException.class, () ->
                searchService.getAllPassports(true, ZonedDateTime.parse("2022-08-04T19:00:00-02:00"),
                        ZonedDateTime.parse("2022-05-04T19:00:00-02:00")));
    }

    @Test
    void testGetAllPassportsWithoutBoolean() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(null, ZonedDateTime.parse("2022-03-05T19:00:00-02:00"),
                        ZonedDateTime.now()));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyStartDate() {
        assertEquals(new ArrayList<>(),
                searchService.getAllPassports(null, null, ZonedDateTime.parse("2022-05-04T19:00:00-02:00")));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyEndDate() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(null, ZonedDateTime.parse("2022-05-04T19:00:00-02:00"), null));
    }

    @Test
    void testGetAllPassportsWithoutParam() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(null, null, null));
    }

    @Test
    void testGetAllPassportsOnlyBoolean() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(true, null, null));
    }

    @Test
    void testFindPersonByPassportNumber() {
        assertEquals(person,
                searchService.findPersonByPassportNumber("1223123113"));
    }
}
