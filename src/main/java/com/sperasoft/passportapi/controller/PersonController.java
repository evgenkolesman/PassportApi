package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public PersonResponse createPerson(@RequestBody @Valid PersonRequest person) {
        return PersonResponse.of(personService.addPerson(Person.of(person)));
    }

    @GetMapping("/{id}")
    public PersonResponse findPersonById(@PathVariable("id") String id) {
        return PersonResponse.of(personService.findById(id));
    }

    @PutMapping("/{id}")
    public PersonResponse updatePerson(@PathVariable("id") String id, @RequestBody @Valid PersonRequest personRequest) {
        return PersonResponse.of(personService.updatePerson(id, Person.of(personRequest)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerson(@PathVariable("id") String id) {
        personService.deletePerson(id);
    }
}
