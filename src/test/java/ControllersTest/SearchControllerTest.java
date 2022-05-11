package ControllersTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.model.NumberPassport;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PassportService;
import com.sperasoft.passportapi.service.PersonService;
import com.sperasoft.passportapi.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class SearchControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PersonRepository personRepository;

    @MockBean
    PassportRepository passportRepository;

    @MockBean
    PersonService personService;

    @MockBean
    PassportService passportService;

    @MockBean
    SearchService searchService;

    private Person person1;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private Passport passport;

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
        person1 = Person.of(personRequest);
        personResponse = PersonResponse.of(person1);

    }

    @Test
    void testFindPersonByPassportNumberCorrect() throws Exception {
        when(passportRepository.getPassportsByParams()).thenReturn(List.of(passport));
        when(personRepository.findAll()).thenReturn(List.of(person1));
        when(searchService.findPersonByPassportNumber("1223123113"))
                .thenReturn(personResponse);
        NumberPassport numberPassport = new NumberPassport();
        numberPassport.setNumber("1223123113");
        ObjectMapper mapper = new ObjectMapper();
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
        this.mvc.perform(post("/searches")
                        .contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllPassports() throws Exception {
        when(searchService.getAllPassports("", "", ""))
                .thenReturn(List.of(passportResponse));
        this.mvc.perform(get("/getAllPassports")
                        .contentType("application/json")
                        .content(personResponse.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }
}