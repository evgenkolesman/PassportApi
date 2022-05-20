package controllerstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportDeactivatedException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportNotFoundException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWasAddedException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import com.sperasoft.passportapi.service.PassportServiceImpl;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class PassportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PersonRepositoryImpl personRepositoryImpl;

    @MockBean
    private PassportRepositoryImpl passportRepository;

    @MockBean
    PersonServiceImpl personService;

    @MockBean
    private PassportServiceImpl passportService;

    @Autowired
    private Environment environment;

    private PassportRequest passportRequest;
    private Person person;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private Passport passport;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate datePassport = LocalDate.parse("2022-05-05", format);
        passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(datePassport);
        passportRequest.setDepartmentCode("123123");
        PersonRequest personRequest = new PersonRequest();
        passport = Passport.of(passportRequest);
        passportResponse = PassportResponse.of(passport);
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = Person.of(personRequest);
        personResponse = PersonResponse.of(person);
    }

    @Test
    void testFindPersonPassportsBooleanTrue() throws Exception {
        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(), "true", "", ""))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport?active=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPersonPassportsBooleanTrueWrongDates() throws Exception {
        when(passportRepository.findPassportById(passport.getId())).thenReturn(passport);
        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(),
                "true", "2022-05-06", "2022-05-05"))
                .thenThrow(new InvalidPassportDataException());
        this.mvc.perform(get("/person/" + personResponse.getId() +
                        "/passport?active=true&dateStart=2022-05-06&dateEnd=2022-05-05")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(Objects.requireNonNull(
                                        environment.getProperty("passport.exception.person.nopassport")),
                                personResponse.getId())));
    }

    @Test
    void testFindPersonPassportsBooleanFalse() throws Exception {
        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(), "false", "", ""))
                .thenReturn(new ArrayList<>());
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport?active=false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testFindPersonPassportsWithoutParams() throws Exception {
        when(passportService.getPassportsByPersonIdAndParams(personResponse.getId(), "", "", ""))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testCreatePassport() throws Exception {
        when(passportService.addPassportToPerson(personResponse.getId(), passportRequest))
                .thenReturn(passportResponse);
        String req = mapper.writer().writeValueAsString(passportRequest);
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testCreatePassportNotCorrect() throws Exception {
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport")
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePassportNotCorrectwBadData() throws Exception {
        PassportRequest passportRequest1 = passportRequest;
        passportRequest1.setNumber("233");
        when(passportService.addPassportToPerson(personResponse.getId(), passportRequest1))
                .thenThrow(new PassportWasAddedException());
        String req = mapper.writer().writeValueAsString(passportRequest1);
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(environment.getProperty("passport.exception.was-added")));
    }

    @Test
    void testFindPassportWithBoolean() throws Exception {
        when(passportService.findPassportById(passport.getId(), "true"))
                .thenReturn(passportResponse);
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport/" +
                        passportResponse.getId() + "?active=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindPassportWithOutBoolean() throws Exception {
        when(passportService.findPassportById(passport.getId(), ""))
                .thenReturn(passportResponse);
        this.mvc.perform(get("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }


    @Test
    void testUpdatePassportNotCorrect() throws Exception {
        this.mvc.perform(put("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePassportNotCorrectNotFound() throws Exception {
        PassportResponse passportForTest = passportResponse;
        passportForTest.setDepartmentCode("111111");
        PassportRequest passportRequestTest = new PassportRequest();
        passportRequestTest.setNumber(passportForTest.getNumber());
        passportRequestTest.setGivenDate(passportForTest.getGivenDate());
        passportRequestTest.setDepartmentCode(passportForTest.getDepartmentCode());
        when(passportService.updatePassport(personResponse.getId(), passportRequestTest))
                .thenThrow(new PersonNotFoundException(personResponse.getId()));
        String req = mapper.writer().writeValueAsString(passportRequestTest);
        this.mvc.perform(put("/person/" + personResponse.getId() + "/passport/" + personResponse.getId())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect((a) ->
                        a.getResponse().getContentAsString()
                                .equals((String.format(Objects.requireNonNull(
                                                environment.getProperty("passport.exception.notfound")),
                                        personResponse.getId()))));
    }

    @Test
    void testUpdatePassportCorrect() throws Exception {
        PassportResponse passportForTest = passportResponse;
        passportForTest.setDepartmentCode("111111");
        PassportRequest passportRequestTest = new PassportRequest();
        passportRequestTest.setNumber(passportForTest.getNumber());
        passportRequestTest.setGivenDate(passportForTest.getGivenDate());
        passportRequestTest.setDepartmentCode(passportForTest.getDepartmentCode());
        when(passportService.findPassportById(passport.getId(), "true"))
                .thenReturn(passportResponse);
        when(passportService.updatePassport(passport.getId(), passportRequestTest))
                .thenReturn(passportForTest);
        String req = mapper.writer().writeValueAsString(passportRequestTest);
        this.mvc.perform(put("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("111111")));
    }

    @Test
    void deletePassport() throws Exception {
        when(personRepositoryImpl.findById(person.getId())).thenReturn(person);
        when(passportRepository.findPassportById(passport.getId())).thenReturn(passport);
        when(passportService.deletePassport(passport.getId()))
                .thenReturn(passportResponse);
        this.mvc.perform(delete("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePassportNotCorrect() throws Exception {
        when(passportService.deletePassport(passport.getId()))
                .thenThrow(new PassportNotFoundException(passport.getId()));
        this.mvc.perform(delete("/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(String.format(Objects.requireNonNull(
                                environment.getProperty("passport.exception.notfound")),
                                passport.getId())
                ));
    }

    @Test
    void lostPassportDeactivate() throws Exception {
        when(personRepositoryImpl.findById(person.getId())).thenReturn(person);
        String req = mapper.writer().writeValueAsString(new Description());
        this.mvc.perform(post("/person/" + personResponse.getId()
                        + "/passport/" + passportResponse.getId() + "/lostPassport?active=false")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void lostPassportDeactivateConflict() throws Exception {
        when(personRepositoryImpl.findById(person.getId())).thenReturn(person);
        when(passportService.deactivatePassport(person.getId(), passport.getId(), false, new Description()))
                .thenThrow(new PassportDeactivatedException());
        String req = mapper.writer().writeValueAsString(new Description());
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport/"
                        + passportResponse.getId() + "/lostPassport?active=false")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(a -> a.getResponse().getContentAsString()
                        .equals(environment.getProperty("passport.exception.deactivated")));
    }
}