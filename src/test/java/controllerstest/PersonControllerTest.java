package controllerstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import com.sperasoft.passportapi.service.PersonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PassportApiApplication.class)
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Environment environment;

    @MockBean
    private PersonRepositoryImpl personRepositoryImpl;

    @MockBean
    private PersonServiceImpl personService;

    private PersonRequest personRequest;
    private Person person;
    private PersonResponse personResponse;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    private void testDataProduce() {
        String string = "2010-02-02";
        LocalDate dateToday = LocalDate.now();
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(dateToday);
        passport.setDepartmentCode("123123");
        personRequest = new PersonRequest();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov");
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        person = Person.of(personRequest);
        personResponse = PersonResponse.of(person);
    }

    @Test
    void testCreatePersonCorrect() throws Exception {
        when(personService.addPerson(personRequest)).thenReturn(personResponse);
        String req = mapper.writeValueAsString(personRequest);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testCreatePersonNotCorrect() throws Exception {
        PersonResponse personResponseForTest = personResponse;
        personResponseForTest.setName("");
        PersonRequest personRequestForTest = personRequest;
        personRequestForTest.setName("");
        when(personService.isPersonPresent(personRequestForTest)).thenReturn(true);
        when(personService.addPerson(personRequestForTest))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        environment.getProperty("person.exception.invalid-data")));
        String req = mapper.writer().writeValueAsString(personRequestForTest);
        this.mvc.perform(post("/person").contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(a -> a.getResponse().getErrorMessage()
                        .equals(environment.getProperty("person.exception.invalid-data")));

    }

    @Test
    void testFindPersonById() throws Exception {
        when(personService.findById(personResponse.getId())).thenReturn(personResponse);
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(get("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alex Frolov")));
    }

    @Test
    void testFindPersonByIdNotCorrect() throws Exception {
        when(personService.findById("23"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(Objects.requireNonNull(
                                environment.getProperty("passport.exception.notfound")), "23")));
        this.mvc.perform(get("/person/23").contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getErrorMessage()
                        .equals(environment.getProperty("person.exception.notfound")));
    }

    @Test
    void testUpdatePersonCorrect() throws Exception {
        PersonRequest personRequestForTest = personRequest;
        personRequestForTest.setName("AAAAAAA");
        Person personNew = Person.of(personRequest);
        PersonResponse personResponseForTest = PersonResponse.of(personNew);

        when(personRepositoryImpl.findPersonById(personResponseForTest.getId())).thenReturn(personNew);
        when(personService.findById(personResponse.getId())).thenReturn(personResponseForTest);
        when(personService.updatePerson(person.getId(), personRequestForTest)).thenReturn(personResponseForTest);
        String req = mapper.writer().writeValueAsString(personRequestForTest);
        this.mvc.perform(put("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AAAAAAA")));
    }

    @Test
    void testUpdatePersonNotCorrect() throws Exception {
        String req = mapper.writer().writeValueAsString("");
        this.mvc.perform(put("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePersonCorrect() throws Exception {
        String req = mapper.writer().writeValueAsString(personRequest);
        this.mvc.perform(delete("/person/" + personResponse.getId()).contentType("application/json")
                        .content(req))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePersonNotCorrect() throws Exception {
        when(personService.deletePerson("23"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(Objects.requireNonNull(
                                environment.getProperty("person.exception.notfound")), "23")));
        this.mvc.perform(delete("/person/23").contentType("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(a -> a.getResponse().getErrorMessage()
                        .equals(environment.getProperty("person.exception.notfound")));
    }
}