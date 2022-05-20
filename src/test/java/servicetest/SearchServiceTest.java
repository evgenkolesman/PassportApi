package servicetest;

import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PassportServiceImpl;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import com.sperasoft.passportapi.service.SearchServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@SpringBootTest(classes = PassportApiApplication.class)
public class SearchServiceTest {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PassportRepository passportRepository;
    @Autowired
    private PassportServiceImpl passportService;
    @Autowired
    private SearchServiceImpl searchService;

    @Autowired
    private PersonServiceImpl personService;

    private PassportResponse passportResponse;
    private Person person;
    private PersonResponse personResponse;
    private final String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @BeforeEach
    private void testDataProduce() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        PassportRequest passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(LocalDate.now());
        passportRequest.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        personResponse = personService.addPerson(personRequest);
        person = Person.of(personRequest);
        person.setId(personResponse.getId());
        passportResponse = passportService.addPassportToPerson(person.getId(), passportRequest);
    }

    @AfterEach
    private void testDataClear() {
        passportRepository.deletePassport(passportResponse.getId());
        personRepository.deletePerson(personResponse.getId());
    }

    @Test
    public void findPersonByPassportNumberTest() {
        assertThat("Problems with adding person", searchService.findPersonByPassportNumber("1223123113")
                .equals(PersonResponse.of(person)));
    }

    @Test
    void testGetAllPassportsAllParams() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("true", "2022-03-05", todayDate));
    }

    @Test
    void testGetAllPassportsBadDate() {
        assertThrowsExactly(InvalidPassportDataException.class, () ->
                searchService.getAllPassports("true", "2022-08-04", "2022-05-04"));
    }

    @Test
    void testGetAllPassportsWithoutBoolean() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("", "2022-03-05", todayDate));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyStartDate() {
        assertEquals(new ArrayList<>(),
                searchService.getAllPassports("", "", "2022-05-04"));
    }

    @Test
    void testGetAllPassportsWithoutBooleanWithEmptyEndDate() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("", "2022-05-04", ""));
    }

    @Test
    void testGetAllPassportsWithoutParam() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("", "", ""));
    }

    @Test
    void testGetAllPassportsOnlyBoolean() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                searchService.getAllPassports("true", "", ""));
    }

    @Test
    void testFindPersonByPassportNumber() {
        assertEquals(personResponse,
                searchService.findPersonByPassportNumber("1223123113"));
    }
}
