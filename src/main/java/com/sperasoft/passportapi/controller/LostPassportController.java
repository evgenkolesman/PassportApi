package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.service.LostPassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("person/{personId}/passport/{id}/lostPassport")
@RequiredArgsConstructor
public class LostPassportController {

    private final LostPassportService service;

    @PatchMapping
    public boolean lostPassportDeactivate(@PathVariable String personId,
                                          @PathVariable String id,
                                          @RequestParam boolean active) {
        return service.deactivatePassport(personId, id, active);
    }
}
