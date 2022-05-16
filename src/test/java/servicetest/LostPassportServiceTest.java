package servicetest;

import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.LostPassportService;
import com.sperasoft.passportapi.service.LostPassportServiceImpl;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = PassportApiApplication.class)
public class LostPassportServiceTest {

    private final PersonRepository personRepository = new PersonRepository();
    private final PassportRepository passportRepository = new PassportRepository();
    private final LostPassportService lostPassportService = new LostPassportServiceImpl(personRepository);
    private Person person;

    @BeforeEach
    private void testDataProduce() {
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(LocalDate.now());
        passport.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        personRepository.addPerson(personRequest);
        person = personRepository.findAll().get(0);
        passportRepository.addPassport(passport, person);
    }


    @Test
    public void testDeactivatePassportCorrect() {
        assertTrue(lostPassportService.deactivatePassport(person.getId(),
                        person.getList().get(0).getId(), false, new Description()),
                "Problems with deactivating passport");
    }

    @Test
    public void testDeactivatePassportNotCorrect() {
        Passport passportWithMistake = person.getList().get(0);
        passportWithMistake.setActive(false);
        assertThrowsExactly(ResponseStatusException.class, () ->
                        lostPassportService.deactivatePassport(person.getId(),
                                person.getList().get(0).getId(), false, new Description()),
                "Passport was deactivated but you try to deactivate it");
    }
}
