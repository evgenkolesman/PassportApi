package com.sperasoft.passportapi.service;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportNotFoundException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
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
    @Autowired
    private BiPredicate<List<Passport>, List<Passport>> predicateList;

    private Passport passport;
    private Person person;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        PassportRequest passportRequest = new PassportRequest("1223123113",Instant.now(),"123123");
        PersonRequest personRequest = new PersonRequest("Alex Frolov", date, "UK");
        person = personService.addPerson(new Person(FriendlyId.createFriendlyId(),
                personRequest.getName(),
                personRequest.getBirthday(),
                personRequest.getBirthdayCountry()));
        passport = passportService.createPassport(new Passport(FriendlyId.createFriendlyId(),
                person.getId(),
                passportRequest.getNumber(),
                passportRequest.getGivenDate(),
                passportRequest.getDepartmentCode()));
    }

    @AfterEach
    private void testDataClear() {
        try {
            passportRepository.deletePassport(passport.getId());
        } catch (PassportNotFoundException e) {
            log.info("Passport was deleted " + passport.getId());
        }
        try {
            personRepository.deletePerson(person.getId());
        } catch (PersonNotFoundException e) {
            log.info("Person was deleted " + person.getId());
        }
    }

    @Test
    public void findPersonByPassportNumberTest() {
        assertThat("Problems with adding person", searchService.findPersonByPassportNumber("1223123113")
                .equals(person));
    }

    @Test
    void testGetAllPassportsAllParams() {
        assertTrue(predicateList.test(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(true,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-03-05T19:00:00-02:00")),
                        Instant.now())));
    }

    @Test
    void testGetAllPassportsBadDate() {
        assertThrowsExactly(InvalidPassportDataException.class, () ->
                searchService.getAllPassports(true,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-08-04T19:00:00-02:00")),
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-05-04T19:00:00-02:00"))));
    }

    @Test
    void testGetAllPassportsWithoutBoolean() {
        assertTrue(predicateList.test(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(null,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-03-05T19:00:00-02:00")),
                        Instant.now())));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyStartDate() {
        assertEquals(new ArrayList<>(),
                searchService.getAllPassports(null, null,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-05-04T19:00:00-02:00"))));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyEndDate() {
        assertTrue(predicateList.test(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(null,
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-05-04T19:00:00-02:00")),
                        null)));
    }

    @Test
    void testGetAllPassportsWithoutParam() {
        assertTrue(predicateList.test(new ArrayList<>(Collections.singleton(passport)),
                searchService.getAllPassports(null, null, null)));
    }

    @Test
    void testGetAllPassportsOnlyBoolean() {
        assertTrue(predicateList.test(List.of(passport),
                searchService.getAllPassports(true, null, null)));
    }

    @Test
    void testFindPersonByPassportNumber() {
        assertEquals(person,
                searchService.findPersonByPassportNumber("1223123113"));
    }
}
