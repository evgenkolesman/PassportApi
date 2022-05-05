package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public PersonResponse createPerson(@RequestBody @Valid PersonRequest person) {
        return personService.addPerson(person);
    }

    @GetMapping("/{id}")
    public PersonResponse findPersonById(@PathVariable String id) {
        return personService.findById(id);
    }

    @PutMapping("/{id}")
    public PersonResponse updatePerson(@PathVariable String id, @RequestBody @Valid PersonRequest person) {
        return personService.updatePerson(id, person);
    }

    @DeleteMapping("/{id}")
    public PersonResponse deletePerson(@PathVariable String id) {
        return personService.deletePerson(id);
    }
}
