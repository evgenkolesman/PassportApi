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

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class PassportServiceTest {

    private final PersonRepository personRepository = new PersonRepository();
    private final PassportRepository passportRepository = new PassportRepository();
    private PassportService passportService = new PassportServiceImpl(passportRepository, personRepository);
    private PassportRequest passport;
    private Person person;
    private PassportResponse passportResponse;
    private String todayDate;

    @BeforeEach
    private void testDataProduce() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        todayDate = LocalDate.now().format(format);
        passportService = new PassportServiceImpl(passportRepository, personRepository);
        passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(LocalDate.now());
        passport.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        personRepository.addPerson(personRequest);
        person = personRepository.findAll().get(0);
        passportResponse = passportService.addPassportToPerson(person.getId(), passport);
    }

    @Test
    public void testAddPassportToPersonCorrect() {
        assertEquals(passportResponse.getNumber(), person.getList().get(0).getNumber());
    }

    @Test
    public void testAddPassportToPersonNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class, () -> passportService.addPassportToPerson(person.getId(), passport));
    }


    @Test
    void testFindPassportById() {
        assertEquals(passportService.findPassportById(person.getId(),
                passportResponse.getId(), "true"), passportResponse);
    }

    @Test
    void testFindPassportByIdInvalidPassport() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.findPassportById(person.getId(), person.getId(), "true"));
    }

    @Test
    void testUpdatePassportCorrect() {
        passport.setDepartmentCode("288");
        assertEquals(passportService.updatePassport(person.getId(),
                passportResponse.getId(), passport).getDepartmentCode(), "288");
    }

    @Test
    void testUpdatePassportNotCorrect() {
        passport.setDepartmentCode("288");
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.updatePassport(person.getId(),
                        "231", passport));
    }

    @Test
    void testDeletePassportCorrect() {
        assertEquals(passportService.deletePassport(person.getId(),
                passportResponse.getId()), passportResponse);
    }

    @Test
    void testDeletePassportNotCorrect() {
        assertThrowsExactly(ResponseStatusException.class,
                () -> passportService.deletePassport(person.getId(),
                        "23123"));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithoutParams() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutBoolean() throws ParseException {
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
    void testGetPassportsByPersonIdAndParamsWithOutDate() throws ParseException {
        assertEquals(new ArrayList<>(Collections.singleton(passportResponse)),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "true", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithOutDateWithFalse() throws ParseException {
        assertEquals(new ArrayList<>(),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "false", "", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithStartDate() throws ParseException {
        assertEquals(List.of(passportResponse),
                passportService.getPassportsByPersonIdAndParams(person.getId(),
                        "true", "2022-05-05", ""));
    }

    @Test
    void testGetPassportsByPersonIdAndParamsWithEndDate() throws ParseException {
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

}