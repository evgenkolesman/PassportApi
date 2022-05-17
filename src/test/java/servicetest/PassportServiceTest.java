package servicetest;

import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import com.sperasoft.passportapi.service.PassportServiceImpl;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PassportApiApplication.class)
class PassportServiceTest {

    @Autowired
    private PersonRepositoryImpl personRepositoryImpl;
    @Autowired
    private PassportRepositoryImpl passportRepository;
    @Autowired
    private PassportServiceImpl passportService;
    @Autowired
    private PersonServiceImpl personService;
    private PassportRequest passportRequest;
    private Person person;
    private PassportResponse passportResponse;
    private String todayDate;
    Passport passport;

    @BeforeEach
    private void testDataProduce() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        todayDate = LocalDate.now().format(format);
        passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(LocalDate.now());
        passportRequest.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        PersonResponse personResponse = personService.addPerson(personRequest);
        person = Person.of(personRequest);
        person.setId(personResponse.getId());
        passportResponse = passportService.addPassportToPerson(person.getId(), passportRequest);
        passport = passportRepository.findPassportById(passportResponse.getId());
        passport.setId(passportResponse.getId());
        person.getList().add(passport);
    }

    @AfterEach
    private void testDataClear() {
        passportRepository.deletePassport(passportResponse.getId());
        personRepositoryImpl.deletePerson(person.getId());
    }

    @Test
    public void testAddPassportToPersonCorrect() {
        assertEquals(passportResponse.getNumber(), person.getList().get(0).getNumber());
    }

    @Test
    public void testAddPassportToPersonNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.addPassportToPerson(person.getId(), passportRequest));
    }


    @Test
    void testFindPassportById() {
        assertEquals(passportService.findPassportById(passportResponse.getId(), "true"), passportResponse);
    }

    @Test
    void testFindPassportByIdInvalidPassport() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.findPassportById(person.getId(), "true"));
    }

    @Test
    void testUpdatePassportCorrect() {
        PassportRequest passportRequest1 = passportRequest;
        passportRequest1.setNumber("2133548212");
        passportRequest1.setDepartmentCode("213123");
        passportRequest1.setGivenDate(LocalDate.now());
        assertEquals(passportService.updatePassport(passportResponse.getId(),
                passportRequest1).getDepartmentCode(), passportRequest1.getDepartmentCode(),
                "Update problems with department code");
        assertEquals(passportService.updatePassport(passportResponse.getId(),
                        passportRequest1).getNumber(), passportRequest1.getNumber(),
                "Update problems with number");
        assertEquals(passportService.updatePassport(passportResponse.getId(),
                        passportRequest1).getGivenDate(), passportRequest1.getGivenDate(),
                "Update problems with given date");
    }

    @Test
    void testUpdatePassportNotCorrect() {
        passportRequest.setDepartmentCode("288");
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.updatePassport("231", passportRequest),
                "wrong id passed need to check");
    }

    @Test
    void testDeletePassportCorrect() {
        assertEquals(passportService.deletePassport(passportResponse.getId()), passportResponse);
    }

    @Test
    void testDeletePassportNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.deletePassport("23123"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithoutParams() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBoolean() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "", "2022-03-05", todayDate));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBooleanWrong() {
        assertThrowsExactly(ResponseStatusException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "", "2022-08-05", "2022-05-04"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDate() {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "true", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDateWithFalse() {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "false", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithStartDate() {
        assertEquals(List.of(passportResponse),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "true", "2022-05-05", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithEndDate() {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "true", "", "2022-04-05"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithBadDate() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "true", "2022-08-04", "2022-04-05"));
    }


    @Test
    public void testDeactivatePassportCorrect() {
        assertTrue(passportService.deactivatePassport(person.getId(),
                        person.getList().get(0).getId(), false, new Description()),
                "Problems with deactivating passport");
    }

    @Test
    public void testDeactivatePassportNotCorrect() {
        Person person1 = personRepositoryImpl.findById(person.getId());
        person1.getList().get(0).setActive(false);
        assertThrowsExactly(ResponseStatusException.class, () ->
                        passportService.deactivatePassport(person1.getId(),
                        person1.getList().get(0).getId(), false, new Description()),
                "Passport should be deactivated but not");
    }
}