package com.sperasoft.passportapi.service;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PassportServiceTest {

    @Autowired
    private PersonRepositoryImpl personRepositoryImpl;
    @Autowired
    private PassportRepositoryImpl passportRepository;
    @Autowired
    private PassportService passportService;
    @Autowired
    private PersonService personService;

    private Person person;
    private PassportRequest passportRequest;
    private PersonRequest personRequest;
    private Passport passport;

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        passportRequest = new PassportRequest("1223123113", Instant.now(), "123123");
        personRequest = new PersonRequest("Alex Frolov", date, "UK");
        person = personService.addPerson(Person.of(FriendlyId.createFriendlyId(), personRequest));
        passport = passportService.addPassportToPerson(person.getId(),
                Passport.of(FriendlyId.createFriendlyId(), passportRequest));
    }

    @AfterEach
    private void testDataClear() {
        passportRepository.deletePassport(passport.getId());
        personRepositoryImpl.deletePerson(person.getId());
    }


    //TODO fix tests with controller mocks maybe can replace services and repos
    @Test
    public void testAddPassportToPersonCorrect() {
        assertEquals(passport.getNumber(), person.getList().get(0).getNumber());
    }

    @Test
    public void testAddPassportToPersonNotCorrect() {
        assertThrowsExactly(PassportWasAddedException.class,
                () -> passportService.addPassportToPerson(person.getId(), passport));
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
        Passport passport1 = Passport.of(FriendlyId.createFriendlyId(), passportRequest1);
        assertEquals(passportService.updatePassport(person.getId(), passport.getId(),
                        passport1).getDepartmentCode(), passportRequest1.getDepartmentCode(),
                "Update problems with department code");
        assertEquals(passportService.updatePassport(person.getId(), passport.getId(),
                        passport1).getNumber(), passportRequest1.getNumber(),
                "Update problems with number");
        assertEquals(passportService.updatePassport(person.getId(), passport.getId(),
                        passport1).getGivenDate(), passportRequest1.getGivenDate(),
                "Update problems with given date");
    }

    @Test
    void testUpdatePassportNotCorrect() {
        Passport passport = new Passport(this.passport.getId(), this.passport.getNumber(),
                this.passport.getGivenDate(), "288");
        assertThrowsExactly(PassportNotFoundException.class,
                () -> passportService.updatePassport(FriendlyId.createFriendlyId(), FriendlyId.createFriendlyId(), passport),
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

    @Test
    void testGetPassportsByPersonIdAndParamsWithStartDate() {
        person.getList().clear();
        assertThrowsExactly(PassportEmptyException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, null, null));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithEndDate() {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, null,
                        Instant.parse("2022-05-11T19:00:00-02:00")));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutPassport() {
        Person person1 = new Person(FriendlyId.createFriendlyId(), "Elkin Vasiliy", LocalDate.now(), "Ru");
        Person person2 = personService.addPerson(person1);
        assertThrowsExactly(PassportEmptyException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person2.getId(),
                        true, null, Instant.parse("2022-04-05T19:00:00-02:00")));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithBadDate() {
        assertThrowsExactly(InvalidPassportDataException.class,
                () -> passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, Instant.parse("2022-08-04T19:00:00+02:00"),
                        Instant.parse("2022-04-05T19:00:00+02:00")));
    }


    @Test
    public void testDeactivatePassportCorrect() {
        assertTrue(passportService.deactivatePassport(person.getId(),
                        person.getList().get(0).getId(), false, new Description("NO DESC")),
                "Problems with deactivating passport");
    }

    @Test
    public void testDeactivatePassportNotCorrect() {
        Person person1 = personRepositoryImpl.findById(person.getId());
        Passport passport2 = passportRepository.updatePassport(person1, new Passport(passport.getId(),
                passport.getNumber(), passport.getGivenDate(), passport.getDepartmentCode(), false, passport.getDescription()));
        person1.getList().remove(0);
        person1.getList().add(passport2);
        assertThrowsExactly(PassportDeactivatedException.class, () ->
                        passportService.deactivatePassport(person1.getId(),
                                person1.getList().get(0).getId(),
                                false,
                                new Description("New Desc")),
                "Passport should be deactivated but not");
    }
}