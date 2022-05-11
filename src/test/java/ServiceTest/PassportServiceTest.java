package ServiceTest;

import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PassportService;
import com.sperasoft.passportapi.service.PassportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class PassportServiceTest {

    private PersonRepository personRepository = new PersonRepository();

    private PassportRepository passportRepository = new PassportRepository();
    private PassportService passportService = new PassportServiceImpl(passportRepository, personRepository);
    PassportRequest passport;
    PersonRequest personRequest;
    Person person1;
    PassportResponse passportResponse;

    @BeforeEach
    private void testDataProduce() throws ParseException {
        passportService = new PassportServiceImpl(passportRepository, personRepository);
        passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(new Date());
        passport.setDepartmentCode("123123");
        personRequest = new PersonRequest();
        String string = "2010-2-2";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(string);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        personRepository.addPerson(personRequest);
        person1 = personRepository.findAll().get(0);
        passportResponse = passportService.addPassportToPerson(person1.getId(), passport);
    }

    @Test
    public void testAddPassportToPersonCorrect() {
        assertEquals(passportResponse.getNumber(), person1.getList().get(0).getNumber());
    }

    @Test
    public void testAddPassportToPersonNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class, () -> passportService.addPassportToPerson(person1.getId(), passport));
    }


    @Test
    void testFindPassportById() {
        assertEquals(passportService.findPassportById(person1.getId(),
                passportResponse.getId(), "true"), passportResponse);
    }

    @Test
    void testFindPassportByIdInvalidPassport() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.findPassportById(person1.getId(), person1.getId(), "true"));
    }

    @Test
    void testUpdatePassportCorrect() {
        passport.setDepartmentCode("288");
        assertEquals(passportService.updatePassport(person1.getId(),
                passportResponse.getId(), passport).getDepartmentCode(), "288");
    }

    @Test
    void testUpdatePassportNotCorrect() {
        passport.setDepartmentCode("288");
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.updatePassport(person1.getId(),
                        "231", passport));
    }

    @Test
    void testDeletePassportCorrect() {
        assertEquals(passportService.deletePassport(person1.getId(),
                passportResponse.getId()), passportResponse);
    }

    @Test
    void testDeletePassportNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.deletePassport(person1.getId(),
                        "23123"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithoutParams() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person1.getId(),
                        "", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBoolean() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person1.getId(),
                        "", "2022-03-05", "2022-05-04"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBooleanWrong() {
        assertThrowsExactly(ResponseStatusException.class, () ->
                passportService.getPassportsByPersonIdAndParams(person1.getId(),
                        "", "2022-08-05", "2022-05-04"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDate() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person1.getId(),
                        "true", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDateWithFalse() throws ParseException {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person1.getId(),
                        "false", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithBadDate() {
        assertThrowsExactly(ResponseStatusException.class,
                () ->
                        passportService.getPassportsByPersonIdAndParams(person1.getId(),
                                "true", "2022-08-04", "2022-04-05"));
    }

}