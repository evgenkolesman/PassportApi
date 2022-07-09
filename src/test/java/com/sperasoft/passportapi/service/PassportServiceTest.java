package com.sperasoft.passportapi.service;

import ch.qos.logback.core.pattern.parser.Parser;
import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Description;
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
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class PassportServiceTest {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PassportRepository passportRepository;
    @Autowired
    private PassportService passportService;
    @Autowired
    private PersonService personService;

    private Person person;
    private PassportRequest passportRequest;
    private PersonRequest personRequest;
    private Passport passport;
    private final DateTimeFormatter isoOffsetDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        passportRequest = new PassportRequest("1223123113", Instant.now(), "123123");
        personRequest = new PersonRequest("Alex Frolov", date, "UK");
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
    void testFindPassportById() {
        assertEquals(passportService.findPassportById(passport.getId(), true), passport);
    }

    @Test
    void testFindPassportByIdInvalidPassport() {
        assertThrowsExactly(PassportNotFoundException.class,
                () -> passportService.findPassportById(person.getId(), true));
    }

    @Test
    void testUpdatePassportCorrect() {
        PassportRequest passportRequest1 = new PassportRequest("2133548212", Instant.now(), "213123");
        Passport passport1 = new Passport(passport.getId(),
                person.getId(),
                passportRequest1.getNumber(),
                passportRequest1.getGivenDate(),
                passportRequest1.getDepartmentCode());
        assertEquals(passportService.updatePassport(passport1).getDepartmentCode(),
                passportRequest1.getDepartmentCode(),
                "Update problems with department code");
        assertEquals(passportService.updatePassport(passport1).getNumber(),
                passportRequest1.getNumber(),
                "Update problems with number");
        assertEquals(passportService.updatePassport(passport1).getGivenDate(),
                passportRequest1.getGivenDate(),
                "Update problems with given date");
    }

    @Test
    void testUpdatePassportNotCorrect() {
        Passport passport = new Passport(FriendlyId.createFriendlyId(), FriendlyId.createFriendlyId(), this.passport.getNumber(),
                this.passport.getGivenDate(), "288");
        assertThrowsExactly(PassportNotFoundException.class,
                () -> passportService.updatePassport(passport),
                "wrong id passed need to check");
    }

    @Test
    void testDeletePassportCorrect() {
        assertEquals(passportService.deletePassport(passport.getId()), passport);
    }

    @Test
    void testDeletePassportNotCorrect() {
        assertThrowsExactly(PassportNotFoundException.class,
                () -> passportService.deletePassport("23123"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithoutParams() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        null, null, null));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBoolean() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        null, Instant.parse("2022-05-01T19:00:00-02:00"),
                        Instant.now()));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBooleanWrong() {
        assertThrowsExactly(InvalidPassportDataException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        null, Instant.parse("2022-12-01T19:00:00-02:00"), Instant.parse("2022-05-01T19:00:00-02:00")));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDate() {
        assertEquals(new ArrayList<>(Collections.singleton(passport)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, null, null));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDateWithFalse() {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        false, null, null));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutStartDate() {
        assertEquals(List.of(passport),
                Collections.unmodifiableList(passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, Instant.parse("2022-01-01T19:00:00-02:00"),
                        passport.getGivenDate().plusNanos(10))));
    }

//    @Test
//    void testGetPassportsByPersonIdAndParamsWithStartDate() {
//        assertThrowsExactly(PassportEmptyException.class, () ->
//                passportService.getPassportsByPersonIdAndParams(FriendlyId.createFriendlyId(),
//                        true, null, null));
//    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithEndDate() {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, null,
                        Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2022-05-11T19:00:00-02:00"))));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithBadDate() {
        assertThrowsExactly(InvalidPassportDataException.class,
                () -> passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, Instant.from(isoOffsetDateTime.parse("2022-08-04T19:00:00+02:00")),
                        Instant.parse("2022-04-05T19:00:00+02:00")));
    }


    @Test
    public void testDeactivatePassportCorrect() {
        assertTrue(passportService.deactivatePassport(person.getId(),
                        passportRepository.getPassportsByParams(person.getId(), true).get(0).getId(),
                        new Description("NO DESC")),
                "Problems with deactivating passport");
    }

    @Test
    public void testDeactivatePassportNotCorrect() {
        Person person1 = personRepository.findById(person.getId());
        passportService.deactivatePassport(person1.getId(),
                passportRepository.getPassportsByParams(person.getId(), true).get(0).getId(),
                new Description("New Desc"));
        assertThrowsExactly(PassportDeactivatedException.class, () ->
                        passportService.deactivatePassport(person1.getId(),
                                passportRepository.getPassportsByParams(person.getId(), false).get(0).getId(),
                                new Description("New Desc")),
                "Passport should be deactivated but not");
    }
}