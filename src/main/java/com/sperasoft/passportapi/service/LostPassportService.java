package com.sperasoft.passportapi.service;

public interface LostPassportService {
    boolean deactivatePassport(String personId, String id, boolean active);
}
