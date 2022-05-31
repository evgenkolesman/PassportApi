package com.sperasoft.passportapi.service;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PassportApiApplication.class)
public class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepositoryImpl personRepositoryImpl;

    private Person person;
    private final PersonRequest personRequest = new PersonRequest();

    @BeforeEach
    private void beforeData() {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = personService.addPerson(Person.of(personRequest));
    }

    @AfterEach
    private void afterData() {
        personRepositoryImpl.deletePerson(person.getId());
    }

    @Test
    public void testAddPersonCorrect() {
        assertThat("Problems with adding person (name field)",
                person.getName().equals(personRequest.getName()));
        assertThat("Problems with adding person (birthday field)",
                person.getBirthday().equals(personRequest.getBirthday()));
        assertThat("Problems with adding person (birthday country field)",
                person.getBirthdayCountry().equals(personRequest.getBirthdayCountry()));
    }

    @Test
    public void testAddPersonDataNotCorrect() {
        assertThrowsExactly(InvalidPersonDataException.class,
                () -> personService.addPerson(person),
                "Problems with adding person (add a person twice)");
    }

    @Test
    public void testFindByIdCorrect() {
        Person person = personService.findById(this.person.getId());
        assertThat("Problems with search by id",
                person.equals(this.person));
    }

    @Test
    public void testFindByIdNotCorrect() {
        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.findById("123214-dsfdsf-23"),
                "Problems with FindById (Exception not correct)");
    }

    @Test
    public void testUpdatePersonCorrect() {
        PersonRequest personRequest = new PersonRequest();
        personRequest.setName("Alex Frol");
        LocalDate date = LocalDate.now();
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("US");

        Person pr = personService.updatePerson(person.getId(),
                Person.of(personRequest));
        assertEquals("Alex Frol", pr.getName(), "Problems with updating person name field");
        assertTrue(pr.getBirthday().isEqual(date), "Problems with updating person birthday field");
        assertEquals("US", pr.getBirthdayCountry(), "Problems with updating person birthday country field");
    }

    @Test
    public void testUpdatePersonNotCorrect() {

        String id = FriendlyId.createFriendlyId();
        PersonRequest personUpdate = new PersonRequest();
        personUpdate.setName("Alex Frol");
        personUpdate.setBirthday(LocalDate.now());
        personUpdate.setBirthdayCountry("US");
        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.updatePerson(id, Person.of(personUpdate)),
                "Problems with updating person wrong id" + id + " passed ");
    }

    @Test
    public void testDeletePersonCorrect() {
        assertEquals(personService.deletePerson(person.getId()), person, "Problems with delete");
    }

    @Test
    public void testDeletePersonNotCorrectWithDoubleDelete() {
        personService.deletePerson(person.getId());
        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.deletePerson(person.getId()),
                "Problems with delete can delete twice");
    }

    @Test
    public void testDeletePersonNotCorrectWithBadID() {
        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.deletePerson("123214-dsfdsf-23"),
                "Problems with search by ID: 123214-dsfdsf-23 in delete");
    }

}
