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
import com.sperasoft.passportapi.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @MockBean
    private PersonRepository personRepository;

    @MockBean
    private PassportRepository passportRepository;

    @MockBean
    private SearchService searchService;

    private PassportRequest passportRequest;
    private PersonRequest personRequest;
    private Person person1;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private Passport passport;

    @BeforeEach
    private void testDataProduce() throws ParseException {
        String string = "2010-2-2";
        Date dateToday = new Date();
        passportRequest = new PassportRequest();
        passportRequest.setNumber("1223123113");
        passportRequest.setGivenDate(dateToday);
        passportRequest.setDepartmentCode("123123");
        personRequest = new PersonRequest();
        passport = Passport.of(passportRequest);
        passportResponse = PassportResponse.of(passport);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(string);
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
        ObjectMapper mapper = new ObjectMapper();
        when(searchService.getAllPassports("", "", ""))
                .thenReturn(List.of(passportResponse));
        String req = mapper.writer().writeValueAsString(personResponse);
        this.mvc.perform(get("/getAllPassports")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1223123113")));
    }
}