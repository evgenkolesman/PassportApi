package ServiceTest;

import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PassportService;
import com.sperasoft.passportapi.service.PassportServiceImpl;
import com.sperasoft.passportapi.service.SearchService;
import com.sperasoft.passportapi.service.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class SearchServiceTest {

    private PersonRepository personRepository = new PersonRepository();
    private PassportRepository passportRepository = new PassportRepository();
    private SearchService searchService = new SearchServiceImpl(passportRepository, personRepository);
    private PassportService passportService;

    private PassportResponse passportResponse;
    private PassportRequest passport;
    private PersonRequest person;
    private Person person1;
    private PersonResponse personResponse;


    @BeforeEach
    private void testDataProduce() throws ParseException {
        passportService = new PassportServiceImpl(passportRepository, personRepository);
        passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(new Date());
        passport.setDepartmentCode("123123");
        person = new PersonRequest();
        String string = "2010-2-2";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(string);
        person.setName("Alex Frolov");
        person.setBirthday(date);
        person.setBirthdayCountry("UK");

        person1 =personRepository.addPerson(person);
        personResponse =PersonResponse.of(person1);
        passportResponse = passportService.addPassportToPerson(person1.getId(), passport);
    }

    @Test
    public void findPersonByPassportNumberTest() {
        personRepository.addPerson(person);
        Person person1 = personRepository.findAll().get(0);
        passportRepository.addPassport(passport, person1);
        assertThat("Problems with adding person", searchService.findPersonByPassportNumber("1223123113")
                .equals(PersonResponse.of(person1)));
    }

    @Test
    void testGetAllPassportsAllParams() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("true", "2022-03-05", "2022-05-04"));
    }

    @Test
    void testGetAllPassportsBadDate() {
        assertThrowsExactly(ResponseStatusException.class, () ->
                searchService.getAllPassports( "true", "2022-08-04", "2022-05-04"));
    }

    @Test
    void testGetAllPassportsWithoutBoolean() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports( "", "2022-03-05", "2022-05-04"));
    }

    @Test
    void testGetAllPassportsWithoutParam() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports( "", "", ""));
    }

    @Test
    void testGetAllPassportsOnlyBoolean() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("true", "", ""));
    }

    @Test
    void testFindPersonByPassportNumber() {
        assertEquals(personResponse,
                searchService.findPersonByPassportNumber("1223123113"));
    }
}
