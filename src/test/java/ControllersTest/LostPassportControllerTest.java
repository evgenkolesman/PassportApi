package ControllersTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class LostPassportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PersonRepository personRepository;

    private PassportRequest passportRequest;
    private PersonRequest personRequest;
    private Person person;
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
        person = Person.of(personRequest);
        personResponse = PersonResponse.of(person);
        person.getList().add(passport);

    }


    @Test
    void lostPassportDeactivate() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(personRepository.findById(person.getId())).thenReturn(person);
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
        ObjectMapper mapper = new ObjectMapper();
        person.getList().get(0).setActive(false);
        when(personRepository.findById(person.getId())).thenReturn(person);
        String req = mapper.writer().writeValueAsString(new Description());
        this.mvc.perform(post("/person/" + personResponse.getId() + "/passport/"
                        + passportResponse.getId() + "/lostPassport?active=false")
                        .contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isConflict());
    }
}