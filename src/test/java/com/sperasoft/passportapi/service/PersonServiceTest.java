package com.sperasoft.passportapi.service;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class PersonServiceTest {

    @Autowired
    private PersonService personService;

    private Person person;
    private PersonRequest personRequest;

    @BeforeEach
    private void beforeData() {
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        personRequest = new PersonRequest("Alex Frolov", date, "UK");
        person = personService.addPerson(
                new Person(FriendlyId.createFriendlyId(),
                        personRequest.getName(),
                        personRequest.getBirthday(),
                        personRequest.getBirthdayCountry()));
    }

    @AfterEach
    private void afterData() {
        try {
            personService.deletePerson(person.getId());
        } catch (PersonNotFoundException e) {
            log.info("Person was deleted " + person.getId());
        }
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
        LocalDate date = LocalDate.now();
        PersonRequest personRequest = new PersonRequest("Alex Frol", date, "US");
        Person updatePerson = personService.updatePerson(
                new Person(person.getId(),
                        personRequest.getName(),
                        personRequest.getBirthday(),
                        personRequest.getBirthdayCountry()));
        assertEquals("Alex Frol", updatePerson.getName(), "Problems with updating person name field");
        assertTrue(updatePerson.getBirthday().isEqual(date), "Problems with updating person birthday field");
        assertEquals("US", updatePerson.getBirthdayCountry(), "Problems with updating person birthday country field");
    }

    @Test
    public void testUpdatePersonNotCorrect() {

        String id = FriendlyId.createFriendlyId();
        PersonRequest personUpdate = new PersonRequest("Alex Frol", LocalDate.now(), "US");
        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.updatePerson(new Person(id,
                                personRequest.getName(),
                                personRequest.getBirthday(),
                                personRequest.getBirthdayCountry())),
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
