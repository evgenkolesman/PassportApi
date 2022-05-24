package servicetest;

import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PassportApiApplication.class)
public class PersonServiceTest {

    @Autowired
    private PersonServiceImpl personService;

    @Autowired
    private PersonRepositoryImpl personRepositoryImpl;
    PersonResponse personResponse;
    PersonRequest personRequest = new PersonRequest();

    @BeforeEach
    private void beforeData() {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        personResponse = personService.addPerson(personRequest);
    }

    @AfterEach
    private void afterData() {
        personRepositoryImpl.deletePerson(personResponse.getId());
    }

    @Test
    public void testAddPersonCorrect() {
        assertThat("Problems with adding person (name field)",
                personResponse.getName().equals(personRequest.getName()));
        assertThat("Problems with adding person (birthday field)",
                personResponse.getBirthday().equals(personRequest.getBirthday()));
        assertThat("Problems with adding person (birthday country field)",
                personResponse.getBirthdayCountry().equals(personRequest.getBirthdayCountry()));
    }

    @Test
    public void testAddPersonDataNotCorrect() {
        assertThrowsExactly(InvalidPersonDataException.class,
                () -> personService.addPerson(personRequest),
                "Problems with adding person (add a person twice)");
    }

    @Test
    public void testFindByIdCorrect() {
        PersonResponse personResponse = personService.findById(this.personResponse.getId());
        assertThat("Problems with search by id",
                personResponse.equals(this.personResponse));
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

        PersonResponse pr = personService.updatePerson(personResponse.getId(),
                personRequest);
        assertEquals("Alex Frol", pr.getName(), "Problems with updating person name field");
        assertTrue(pr.getBirthday().isEqual(date),"Problems with updating person birthday field");
        assertEquals("US", pr.getBirthdayCountry(), "Problems with updating person birthday country field");
    }

    @Test
    public void testUpdatePersonNotCorrect() {

        String id = UUID.randomUUID().toString();
        PersonRequest personUpdate = new PersonRequest();
        personUpdate.setName("Alex Frol");
        personUpdate.setBirthday(LocalDate.now());
        personUpdate.setBirthdayCountry("US");

        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.updatePerson(id, personUpdate),
                "Problems with updating person wrong id" + id + " passed ");
    }

    @Test
    public void testDeletePersonCorrect() {
        assertEquals(personService.deletePerson(personResponse.getId()), personResponse, "Problems with delete");
    }

    @Test
    public void testDeletePersonNotCorrectWithDoubleDelete() {
        personService.deletePerson(personResponse.getId());
        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.deletePerson(personResponse.getId()),
                "Problems with delete can delete twice");
    }

    @Test
    public void testDeletePersonNotCorrectWithBadID() {
        assertThrowsExactly(PersonNotFoundException.class, () ->
                        personService.deletePerson("123214-dsfdsf-23"),
                "Problems with search by ID: 123214-dsfdsf-23 in delete");
    }

}
