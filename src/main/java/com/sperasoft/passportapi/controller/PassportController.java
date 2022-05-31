package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.service.PassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/person/{personId}/passport")
@RequiredArgsConstructor
public class PassportController {
    private final PassportService passportService;

    @GetMapping
    public List<PassportResponse> findPersonPassports(@PathVariable(name ="personId") String personId,
                                                      @RequestParam(name = "active") @Nullable Boolean active,
                                                      @RequestParam(name = "dateStart")
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                      @Nullable ZonedDateTime dateStart,
                                                      @RequestParam(name = "dateEnd")
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                      @Nullable ZonedDateTime dateEnd) {
        return passportService.getPassportsByPersonIdAndParams(personId, active, dateStart, dateEnd)
                .stream().map(PassportResponse::of)
                .collect(Collectors.toList());
    }

    @PostMapping
    public PassportResponse createPassport(@PathVariable("personId") String personId,
                                           @RequestBody @Valid PassportRequest passportRequest) {

        return PassportResponse.of(passportService.addPassportToPerson(personId, Passport.of(passportRequest)));
    }

    @GetMapping("/{id}")
    public PassportResponse findPassport(@PathVariable("id") String id,
                                         @RequestParam(name = "active") @Nullable Boolean active) {
        return PassportResponse.of(passportService.findPassportById(id, active));
    }

    @PutMapping("/{id}")
    public PassportResponse updatePassport(@PathVariable("id") String id,
                                           @RequestBody @Valid PassportRequest passportRequest) {
        return PassportResponse.of(passportService.updatePassport(id, Passport.of(passportRequest)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePassport(@PathVariable("id") String id) {
        passportService.deletePassport(id);
    }

    @PostMapping("/{id}/lostPassport")
    public boolean lostPassportDeactivate(@PathVariable("personId") String personId,
                                          @PathVariable("id") String id,
                                          @RequestParam(name = "active") @NotNull Boolean active,
                                          @RequestBody(required = false) Description description) {
        return passportService.deactivatePassport(personId, id, active, description);
    }
}
