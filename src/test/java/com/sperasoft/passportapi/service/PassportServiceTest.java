package com.sperasoft.passportapi.service;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.LostPassportInfo;
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
    @Autowired
    private BiPredicate<Passport, Passport> predicate;
    @Autowired
    private BiPredicate<List<Passport>, List<Passport>> listPredicate;

    private Person person;
    private PassportRequest passportRequest;
    private PersonRequest personRequest;
    private Passport passport;
    private final DateTimeFormatter isoOffsetDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @BeforeEach
    private void testDataProduce() {
        personRequest = new PersonRequest("Alex Frolov", LocalDate.now().minusYears(18), "UK");
        passportRequest = new PassportRequest("1223123113", Instant.now(), "123123");
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
        Passport passportById = passportService.findPassportById(passport.getId(), true);
        assertTrue(predicate.test(passport, passportById));
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
        Passport passportById = passportService.deletePassport(passport.getId());
        assertTrue(predicate.test(passport, passportById));
    }

    @Test
    void testDeletePassportNotCorrect() {
        assertThrowsExactly(PassportNotFoundException.class, () -> passportService.deletePassport("23123"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithoutParams() {
        List<Passport> passportResult = passportService.getPassportsByPersonIdAndParams(person.getId(),
                null, null, null);
        assertTrue(listPredicate.test(new ArrayList<>(Collections.singleton(passport)), passportResult));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBoolean() {
        List<Passport> passportsByPersonIdAndParams = passportService.getPassportsByPersonIdAndParams(person.getId(),
                null, Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-05-01T19:00:00-02:00")),
                Instant.now());
        assertTrue(listPredicate.test(new ArrayList<>(Collections.singleton(passport)),
                passportsByPersonIdAndParams));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBooleanWrong() {
        assertThrowsExactly(InvalidPassportDataException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        null, Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-12-01T19:00:00-02:00")),
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2022-05-01T19:00:00-02:00"))));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDate() {
        List<Passport> passportsByPersonIdAndParams = passportService.getPassportsByPersonIdAndParams(person.getId(),
                true, null, null);
        assertTrue(listPredicate.test(new ArrayList<>(Collections.singleton(passport)),
                passportsByPersonIdAndParams));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDateWithFalse() {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        false, null, null));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutStartDate() {
        List<Passport> passportsByPersonIdAndParams = passportService.getPassportsByPersonIdAndParams(person.getId(),
                true,
                null,
                Instant.now().plusSeconds(10000));
        assertTrue(listPredicate.test(List.of(passport),
                passportsByPersonIdAndParams));
    }


    @Test
    void testGetPassportsByPersonIdAndParamsWithEmptyResult() {
        assertEquals(new ArrayList<>(), passportService.getPassportsByPersonIdAndParams(FriendlyId.createFriendlyId(),
                true, null, null));
    }

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
                        Instant.from(isoOffsetDateTime.parse("2022-04-05T19:00:00+02:00"))));
    }


    @Test
    public void testDeactivatePassportCorrect() {
        assertTrue(passportService.deactivatePassport(person.getId(),
                        passportRepository.getPassportsByParams(person.getId(), true).get(0).getId(),
                        new LostPassportInfo("NO DESC")),
                "Problems with deactivating passport");
    }

    @Test
    public void testDeactivatePassportNotCorrect() {
        Person person1 = personRepository.findById(person.getId());
        passportService.deactivatePassport(person1.getId(),
                passportRepository.getPassportsByParams(person.getId(), true).get(0).getId(),
                new LostPassportInfo("New Desc"));
        assertThrowsExactly(PassportDeactivatedException.class, () ->
                        passportService.deactivatePassport(person1.getId(),
                                passportRepository.getPassportsByParams(person.getId(), false).get(0).getId(),
                                new LostPassportInfo("New Desc")),
                "Passport should be deactivated but not");
    }
}