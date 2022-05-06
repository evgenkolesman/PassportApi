package ControllersTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import com.sperasoft.passportapi.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PersonRepository personRepository;

    @MockBean
    private PersonService personService;

    private PassportRequest passport;
    private PersonRequest personRequest;
    private Person person1;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;

    @BeforeEach
    private void testDataProduce() throws ParseException {
        String string = "2010-2-2";
        Date dateToday = new Date();
        passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(dateToday);
        passport.setDepartmentCode("123123");
        personRequest = new PersonRequest();
        passportResponse = new PassportResponse();

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(string);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person1 = Person.of(personRequest);
        personResponse = PersonResponse.of(person1);

    }

    @Test
    void testCreatePersonCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(personService.addPerson(personRequest)).thenReturn(personResponse);
        String req = mapper.writer().writeValueAsString(personResponse);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));

    }

    @Test
    void testCreatePersonNotCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PersonResponse personResponseForTest = personResponse;
        personResponseForTest.setName("");
        PersonRequest personRequestForTest = personRequest;
        personRequestForTest.setName("");
        when(personRepository.isPersonPresent(personRequestForTest)).thenReturn(true);
        when(personService.addPerson(personRequestForTest))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data"));
        String req = mapper.writer().writeValueAsString(null);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().is4xxClientError());

    }

    @Test
    void testFindPersonById() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(personService.findById(personResponse.getId())).thenReturn(personResponse);
        String req = mapper.writer().writeValueAsString(personResponse);
        this.mvc.perform(get("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testFindPersonByIdNotCorrect() throws Exception {
        when(personService.findById("23"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID"));
        this.mvc.perform(get("/person/23").contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePersonCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PersonRequest personRequestForTest = personRequest;
        personRequestForTest.setName("AAAAAAA");
        Person person = Person.of(personRequest);
        PersonResponse personResponseForTest = PersonResponse.of(person);

        when(personRepository.findPersonById(personResponseForTest.getId())).thenReturn(person);
        when(personService.findById(personResponse.getId())).thenReturn(personResponseForTest);
        when(personService.updatePerson(person1.getId(), personRequestForTest)).thenReturn(personResponseForTest);
        String req = mapper.writer().writeValueAsString(personResponseForTest);
        this.mvc.perform(put("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AAAAAAA")));
    }

    @Test
    void testUpdatePersonNotCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String req = mapper.writer().writeValueAsString(null);
        this.mvc.perform(put("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePersonCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String req = mapper.writer().writeValueAsString(personResponse);
        this.mvc.perform(delete("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(containsString("")));
    }

    @Test
    void testDeletePersonNotCorrect() throws Exception {
        when(personService.findById("23"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data"));
        when(personService.deletePerson("23"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data"));
        this.mvc.perform(delete("/person/23").contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}