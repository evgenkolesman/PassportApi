package ServiceTest;

import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PersonService;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import org.springframework.web.server.ResponseStatusException;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

public class PersonServiceTest {

    private PersonRepository personRepository = new PersonRepository();
    private PersonService personService = new PersonServiceImpl(personRepository);

    @Test
    public void testAddPersonCorrect() throws ParseException {
        PersonRequest person = new PersonRequest();
        String string = "2010-2-2";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(string);
        person.setName("Alex Frolov");
        person.setBirthday(date);
        person.setBirthdayCountry("UK");

        assertThat("Problems with adding person (name field)",
                personService.addPerson(person).getName().equals(person.getName()));
        assertThat("Problems with adding person (birthday field)",
                personService.addPerson(person).getBirthday().equals(person.getBirthday()));
        assertThat("Problems with adding person (birthday country field)",
                personService.addPerson(person).getBirthdayCountry().equals(person.getBirthdayCountry()));
    }

    @Test(expectedExceptions = ResponseStatusException.class)
    public void testAddPersonDataNotCorrect() {
        PersonRequest person = new PersonRequest();
        personService.addPerson(person);
        assertThat("Problems with adding person", !personService.addPerson(person).equals(person));
    }
}
