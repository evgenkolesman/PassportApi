package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.PassportApiApplication;
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PassportApiApplication.class)
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
    private String todayDate;
    private PassportRequest passportRequest;
    private PersonRequest personRequest;
    private Passport passport;

    @BeforeEach
    private void testDataProduce() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        todayDate = LocalDate.now().format(format);
        passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(LocalDateTime.now());
        passportRequest.setDepartmentCode("123123");
        personRequest = new PersonRequest();
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
        PassportRequest passportRequest1 = passportRequest;
        passportRequest1.setNumber("2133548212");
        passportRequest1.setDepartmentCode("213123");
        passportRequest1.setGivenDate(LocalDateTime.now());
        Passport passport1 = Passport.of(passportRequest1);
        passport1.setId(passport1.getId());
        assertEquals(passportService.updatePassport(passport.getId(),
                passport1).getDepartmentCode(), passportRequest1.getDepartmentCode(),
                "Update problems with department code");
        assertEquals(passportService.updatePassport(passport.getId(),
                        passport1).getNumber(), passportRequest1.getNumber(),
                "Update problems with number");
        assertEquals(passportService.updatePassport(passport.getId(),
                        passport1).getGivenDate(), passportRequest1.getGivenDate(),
                "Update problems with given date");
    }

    @Test
    void testUpdatePassportNotCorrect() {
        passport.setDepartmentCode("288");
        assertThrowsExactly(PassportNotFoundException.class,
                () -> passportService.updatePassport("231", passport),
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
                        null, ZonedDateTime.parse("2022-05-01T19:00:00-02:00"),
                        LocalDate.parse(todayDate).atTime(LocalTime.of(0,0,0)).atZone(ZoneId.systemDefault())));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBooleanWrong() {
        assertThrowsExactly(InvalidPassportDataException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        null, ZonedDateTime.parse("2022-12-01T19:00:00-02:00"), ZonedDateTime.parse("2022-05-01T19:00:00-02:00")));
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
    void testGetPassportsByPersonIdAndParamsWithStartDate() {
        assertEquals(List.of(passport),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, null, null));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithEndDate() {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, null,
                        ZonedDateTime.parse("2022-05-11T19:00:00-02:00")));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutPassport() {
        Person person1 = new Person();
        person1.setId("1323jafjsf-3213sdk");
        person1.setName("Elkin Vasiliy");
        person1.setBirthday(LocalDate.now());
        person1.setBirthdayCountry("Ru");
        Person person2 = personService.addPerson(person1);
        assertThrowsExactly(PassportEmptyException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person2.getId(),
                        true, null, ZonedDateTime.parse("2022-04-05T19:00:00-02:00")));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithBadDate() {
        assertThrowsExactly(InvalidPassportDataException.class,
                () -> passportService.getPassportsByPersonIdAndParams(person.getId(),
                        true, ZonedDateTime.parse("2022-08-04T19:00:00-02:00"),
                        ZonedDateTime.parse("2022-04-05T19:00:00-02:00")));
    }


    @Test
    public void testDeactivatePassportCorrect() {
        assertTrue(passportService.deactivatePassport(person.getId(),
                        person.getList().get(0).getId(), false, new Description()),
                "Problems with deactivating passport");
    }

    @Test
    public void testDeactivatePassportNotCorrect() {
        Person person1 = personRepositoryImpl.findById(person.getId());
        person1.getList().get(0).setActive(false);
        assertThrowsExactly(PassportDeactivatedException.class, () ->
                        passportService.deactivatePassport(person1.getId(),
                        person1.getList().get(0).getId(), false, new Description()),
                "Passport should be deactivated but not");
    }
}