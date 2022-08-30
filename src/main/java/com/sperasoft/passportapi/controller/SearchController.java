package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Number;
import com.sperasoft.passportapi.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/searches")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public PersonResponse findPersonByPassportNumber(@NotNull(message = "Invalid data: number filed shouldn`t be empty")
                                                     @RequestBody Number number) {
        return PersonResponse.of(searchService.findPersonByPassportNumber(number.number()));
    }

    @GetMapping
    public List<PassportResponse> findAllPassports(@RequestParam(name = "active")
                                                   @Nullable Boolean active,
                                                   @RequestParam(name = "dateStart")
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                   @Nullable Instant dateStart,
                                                   @RequestParam(name = "dateEnd")
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                   @Nullable Instant dateEnd) {
        return searchService.getAllPassports(active,
                        dateStart,
                        dateEnd)
                .stream()
                .map(PassportResponse::of)
                .collect(Collectors.toList());
    }

}
