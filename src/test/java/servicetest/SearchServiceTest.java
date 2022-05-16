package servicetest;

import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PassportService;
import com.sperasoft.passportapi.service.PassportServiceImpl;
import com.sperasoft.passportapi.service.SearchService;
import com.sperasoft.passportapi.service.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@SpringBootTest(classes = PassportApiApplication.class)
public class SearchServiceTest {

    private final PersonRepository personRepository = new PersonRepository();
    private final PassportRepository passportRepository = new PassportRepository();
    private final SearchService searchService = new SearchServiceImpl(passportRepository, personRepository);

    private PassportResponse passportResponse;
    private Person person;
    private PersonResponse personResponse;
    private final String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @BeforeEach
    private void testDataProduce() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        PassportService passportService = new PassportServiceImpl(passportRepository, personRepository);
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(LocalDate.now());
        passport.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = personRepository.addPerson(personRequest);
        personResponse = PersonResponse.of(person);
        passportResponse = passportService.addPassportToPerson(person.getId(), passport);
    }

    @Test
    public void findPersonByPassportNumberTest() {
        assertThat("Problems with adding person", searchService.findPersonByPassportNumber("1223123113")
                .equals(PersonResponse.of(person)));
    }

    @Test
    void testGetAllPassportsAllParams() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("true", "2022-03-05", todayDate));
    }

    @Test
    void testGetAllPassportsBadDate() {
        assertThrowsExactly(ResponseStatusException.class, () ->
                searchService.getAllPassports("true", "2022-08-04", "2022-05-04"));
    }

    @Test
    void testGetAllPassportsWithoutBoolean() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("", "2022-03-05", todayDate));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyStartDate() throws ParseException {
        assertEquals(new ArrayList<>(),
                searchService.getAllPassports("", "", "2022-05-04"));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyEndDate() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("", "2022-05-04", ""));
    }

    @Test
    void testGetAllPassportsWithoutParam() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("", "", ""));
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
