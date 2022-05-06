package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.service.PassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/person/{personId}")
@RequiredArgsConstructor
public class PassportController {
    private final PassportService passportService;

    @GetMapping("/passport")
    public List<PassportResponse> findPersonPassports(@PathVariable String personId,
                                                      @RequestParam(defaultValue = "") String active,
                                                      @RequestParam(defaultValue = "") String dateStart,
                                                      @RequestParam(defaultValue = "") String dateEnd) throws ParseException {
        return passportService.getPassportsByPersonIdAndParams(personId, active, dateStart, dateEnd);
    }

    @PostMapping("/passport")
    public PassportResponse createPassport(@PathVariable String personId,
                                           @RequestBody @Valid PassportRequest passportRequest) {
        return passportService.addPassportToPerson(personId, passportRequest);
    }

    @GetMapping("/passport/{id}")
    public PassportResponse findPassport(@PathVariable String personId,
                                         @PathVariable String id,
                                         @RequestParam(defaultValue = "") String active) {
        return passportService.findPassportById(personId, id, active);
    }

    @PutMapping("/passport/{id}")
    public PassportResponse updatePassport(@PathVariable String personId,
                                           @PathVariable String id,
                                           @RequestBody @Valid PassportRequest passport) {
        return passportService.updatePassport(personId, id, passport);
    }

    @DeleteMapping("/passport/{id}")
    public ResponseEntity<Void> deletePassport(@PathVariable String personId,
                                                     @PathVariable String id) {
        passportService.deletePassport(personId, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
