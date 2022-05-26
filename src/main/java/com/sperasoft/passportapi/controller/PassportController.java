package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.service.PassportServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/person/{personId}")
@RequiredArgsConstructor
public class PassportController {
    private final PassportServiceImpl passportService;

    @GetMapping("/passport")
    public List<PassportResponse> findPersonPassports(@PathVariable String personId,
                                                      @RequestParam(defaultValue = "") String active,
                                                      @RequestParam(defaultValue = "") String dateStart,
                                                      @RequestParam(defaultValue = "") String dateEnd) {
        return passportService.getPassportsByPersonIdAndParams(personId, active, dateStart, dateEnd);
    }

    @PostMapping("/passport")
    public PassportResponse createPassport(@PathVariable String personId,
                                           @RequestBody @Valid PassportRequest passportRequest) {
        return passportService.addPassportToPerson(personId, passportRequest);
    }

    @GetMapping("/passport/{id}")
    public PassportResponse findPassport(@PathVariable String id,
                                         @RequestParam(defaultValue = "") String active) {
        return passportService.findPassportById(id, active);
    }

    @PutMapping("/passport/{id}")
    public PassportResponse updatePassport(@PathVariable String id,
                                           @RequestBody @Valid PassportRequest passport) {
        return passportService.updatePassport(id, passport);
    }

    @DeleteMapping("/passport/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePassport(@PathVariable String id) {
        passportService.deletePassport(id);
    }

    @PostMapping("/passport/{id}/lostPassport")
    public boolean lostPassportDeactivate(@PathVariable String personId,
                                          @PathVariable String id,
                                          @RequestParam boolean active,
                                          @RequestBody(required = false) Description description) {
        return passportService.deactivatePassport(personId, id, active, description);
    }

}