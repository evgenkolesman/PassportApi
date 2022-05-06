package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.model.NumberPassport;
import com.sperasoft.passportapi.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/searches")
    public PersonResponse findPersonByPassportNumber(@RequestBody NumberPassport number) {
        return searchService.findPersonByPassportNumber(number.getNumber());
    }

    @GetMapping("/getAllPassports")
    public List<PassportResponse> findAllPassports(@RequestParam(defaultValue = "") String active,
                                                   @RequestParam(defaultValue = "") String dateStart,
                                                   @RequestParam(defaultValue = "") String dateEnd) throws ParseException {
        return searchService.getAllPassports(active, dateStart, dateEnd);
    }

}
