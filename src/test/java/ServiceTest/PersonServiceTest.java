package ServiceTest;

import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PersonService;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersonServiceTest {

    private PersonService personService;
    PersonResponse person1;
    PersonRequest person = new PersonRequest();

    @BeforeEach
    private void beforeData() throws ParseException {
        personService = new PersonServiceImpl(new PersonRepository());
        String string = "2010-2-2";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(string);
        person.setName("Alex Frolov");
        person.setBirthday(date);
        person.setBirthdayCountry("UK");
        person1 = personService.addPerson(person);

    }

    @Test
    public void testAddPersonCorrect() {
        assertThat("Problems with adding person (name field)",
                person1.getName().equals(person.getName()));
        assertThat("Problems with adding person (birthday field)",
                person1.getBirthday().equals(person.getBirthday()));
        assertThat("Problems with adding person (birthday country field)",
                person1.getBirthdayCountry().equals(person.getBirthdayCountry()));
    }

    @Test
    public void testAddPersonDataNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> personService.addPerson(person).equals(person),
                "Problems with adding person (add a person twice)");
    }

    @Test
    public void testFindByIdCorrect() {
        PersonResponse personResponse = personService.findById(person1.getId());
        assertThat("Problems with search by id",
                personResponse.equals(person1));
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
        person.setBirthday(new Date());
        person.setBirthdayCountry("US");

        assertThat("Problems with updating person",
                !personService.updatePerson(person1.getId(), person).getName().equals(person1.getName()));
    }

    @Test
    public void testUpdatePersonNotCorrect() {

        String id = UUID.randomUUID().toString();
        PersonRequest personUpdate = new PersonRequest();
        personUpdate.setName("Alex Frol");
        personUpdate.setBirthday(new Date());
        personUpdate.setBirthdayCountry("US");

        assertThrowsExactly(ResponseStatusException.class, () ->
                        personService.updatePerson(id, personUpdate).getName().equals(personUpdate.getName()),
                "Problems with updating person (name field)");
    }

    @Test
    public void testDeletePersonCorrect() {
        assertTrue(personService.deletePerson(person1.getId()).equals(person1),
                "Problems with delete");
    }

    @Test
    public void testDeletePersonNotCorrectWithDoubleDelete() {

        personService.deletePerson(person1.getId()).equals(person1);
        assertThrowsExactly(ResponseStatusException.class, () ->
                        personService.deletePerson(person1.getId()).equals(person1),
                "Problems with delete can delete twice");
    }

    @Test
    public void testDeletePersonNotCorrectWithBadID() {
        assertThrowsExactly(ResponseStatusException.class, () ->
                        personService.deletePerson("123214-dsfdsf-23"),
                "Problems with search by id in delete");
    }

}
