package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/person/{id}")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/search")
    public PersonResponse findPersonByPassportNumber(@PathVariable String id,
                                                     @RequestBody String number) {
        return searchService.findPersonByPassportNumber(number);
    }

}
