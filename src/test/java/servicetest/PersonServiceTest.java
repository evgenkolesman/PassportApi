package servicetest;

import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PersonService;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PassportApiApplication.class)
public class PersonServiceTest {

    private PersonService personService;
    PersonResponse person;
    PersonRequest personRequest = new PersonRequest();

    @BeforeEach
    private void beforeData() {
        personService = new PersonServiceImpl(new PersonRepository());
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = personService.addPerson(personRequest);
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
        assertThrowsExactly(ResponseStatusException.class,
                () -> personService.addPerson(personRequest),
                "Problems with adding person (add a person twice)");
    }

    @Test
    public void testFindByIdCorrect() {
        PersonResponse personResponse = personService.findById(person.getId());
        assertThat("Problems with search by id",
                personResponse.equals(person));
    }

    @Test
    public void testFindByIdNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class, () ->
                        personService.findById("123214-dsfdsf-23"),
                "Problems with FindById (Exception not correct)");
    }

    @Test
    public void testUpdatePersonCorrect() {
        person.setName("Alex Frol");
        person.setBirthday(LocalDate.now());
        person.setBirthdayCountry("US");

        assertThat("Problems with updating person",
                !personService.updatePerson(person.getId(),
                        personRequest).getName().equals(person.getName()));
    }

    @Test
    public void testUpdatePersonNotCorrect() {

        String id = UUID.randomUUID().toString();
        PersonRequest personUpdate = new PersonRequest();
        personUpdate.setName("Alex Frol");
        personUpdate.setBirthday(LocalDate.now());
        personUpdate.setBirthdayCountry("US");

        assertThrowsExactly(ResponseStatusException.class, () ->
                        personService.updatePerson(id, personUpdate),
                "Problems with updating person (name field)");
    }

    @Test
    public void testDeletePersonCorrect() {
        assertEquals(personService.deletePerson(person.getId()), person, "Problems with delete");
    }

    @Test
    public void testDeletePersonNotCorrectWithDoubleDelete() {
        personService.deletePerson(person.getId());
        assertThrowsExactly(ResponseStatusException.class, () ->
                        personService.deletePerson(person.getId()),
                "Problems with delete can delete twice");
    }

    @Test
    public void testDeletePersonNotCorrectWithBadID() {
        assertThrowsExactly(ResponseStatusException.class, () ->
                        personService.deletePerson("123214-dsfdsf-23"),
                "Problems with search by id in delete");
    }

}
