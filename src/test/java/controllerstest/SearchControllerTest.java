package controllerstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWrongNumberException;
import com.sperasoft.passportapi.model.NumberPassport;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import com.sperasoft.passportapi.service.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class SearchControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment environment;

    @MockBean
    private PersonRepositoryImpl personRepositoryImpl;

    @MockBean
    private PassportRepositoryImpl passportRepository;

    @MockBean
    private SearchServiceImpl searchService;

    private Person person;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private Passport passport;
    private final ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateToday = LocalDate.now();
        PassportRequest passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(dateToday);
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
    void testFindPersonByPassportNumberCorrect() throws Exception {
        when(passportRepository.getPassportsByParams()).thenReturn(List.of(passport));
        when(personRepositoryImpl.findAll()).thenReturn(List.of(person));
        when(searchService.findPersonByPassportNumber("1223123113"))
                .thenReturn(personResponse);
        NumberPassport numberPassport = new NumberPassport();
        numberPassport.setNumber("1223123113");
        String req = mapper.writer().writeValueAsString(numberPassport);
        this.mvc.perform(post("/searches")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }


    @Test
    void testFindPersonByPassportNumberNotCorrect() throws Exception {
        when(personRepositoryImpl.findAll()).thenReturn(Collections.singletonList(person));
        when(searchService.findPersonByPassportNumber("2313"))
                .thenThrow(new PassportWrongNumberException());
        NumberPassport number = new NumberPassport();
        number.setNumber("2313");
        String req = mapper.writer().writeValueAsString(number);
        this.mvc.perform(post("/searches")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString().
                        equals(environment.getProperty("searches.exception.wrong-num")));

    }

    @Test
    void testFindAllPassportsCorrect() throws Exception {
        when(searchService.getAllPassports("", "", ""))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/searches")
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }

    @Test
    void testFindAllPassportsCorrectBadDates() throws Exception {
        when(searchService.getAllPassports("", "15-05-2022", "10-05-2022"))
                .thenThrow(new InvalidPassportDataException());
        this.mvc.perform(get("/searches?dateStart=15-05-2022&dateEnd=10-05-2022")
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getContentAsString().equals(
                        environment.getProperty("passport.exception.invalid.date")));
    }
}