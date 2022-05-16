package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PersonResponse> deletePerson(@PathVariable String id) {
        personService.deletePerson(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
