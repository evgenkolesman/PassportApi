package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    private final PassportRepository passportRepository;
    private final PersonRepository personRepository;

    @Override
    public PersonResponse findPersonByPassportNumber(String number) {
        return passportRepository.getPassportsByParams().stream()
        .filter(passport -> passport.getNumber().equals(number))
        .map(passport ->  personRepository.findAll().stream()
        .filter(person ->
                person.getList().stream().anyMatch(
                        p -> p.getNumber().equals(number))).findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "No such number")))
        .map(PersonResponse::of)
                .findFirst().get();
    }
}
