package ServiceTest;

import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.SearchService;
import com.sperasoft.passportapi.service.SearchServiceImpl;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

public class SearchServiceTest {

    PersonRepository personRepository = new PersonRepository();
    PassportRepository passportRepository = new
            PassportRepository();

    SearchService searchService = new SearchServiceImpl(passportRepository, personRepository);

    @Test
    public void findPersonByPassportNumberTest() throws ParseException {
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(new Date());
        passport.setDepartmentCode("123123");
        PersonRequest person = new PersonRequest();
        String string = "2010-2-2";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(string);
        person.setName("Alex Frolov");
        person.setBirthday(date);
        person.setBirthdayCountry("UK");
        personRepository.addPerson(person);
        Person person1 = personRepository.findAll().get(0);
        passportRepository.addPassport(passport, person1);
        assertThat("Problems with adding person", searchService.findPersonByPassportNumber("1223123113")
                .equals(PersonResponse.of(person1)));
    }
}
