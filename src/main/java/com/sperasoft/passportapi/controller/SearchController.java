package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.NumberPassport;
import com.sperasoft.passportapi.service.SearchServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/searches")
@RequiredArgsConstructor
public class SearchController {

    private final SearchServiceImpl searchService;

    @PostMapping
    public PersonResponse findPersonByPassportNumber(@RequestBody NumberPassport number) {
        return searchService.findPersonByPassportNumber(number.getNumber());
    }

    @GetMapping
    public List<PassportResponse> findAllPassports(@RequestParam(defaultValue = "") String active,
                                                   @RequestParam(defaultValue = "") String dateStart,
                                                   @RequestParam(defaultValue = "") String dateEnd) {
        return searchService.getAllPassports(active, dateStart, dateEnd);
    }

}
