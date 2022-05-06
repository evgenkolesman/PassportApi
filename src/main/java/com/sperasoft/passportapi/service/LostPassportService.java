package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.model.Description;

public interface LostPassportService {
    boolean deactivatePassport(String personId, String id, boolean active, Description description);
}
