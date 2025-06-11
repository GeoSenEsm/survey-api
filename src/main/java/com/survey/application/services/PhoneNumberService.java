package com.survey.application.services;

import com.survey.application.dtos.PhoneNumberDtoIn;
import com.survey.application.dtos.PhoneNumberDtoOut;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhoneNumberService {
    PhoneNumberDtoOut createPhoneNumber(PhoneNumberDtoIn phoneNumberDtoIn);
    List<PhoneNumberDtoOut> getAllPhoneNumbers();
    Optional<PhoneNumberDtoOut> getPhoneNumberById(UUID id);
    PhoneNumberDtoOut updatePhoneNumber(UUID id, PhoneNumberDtoIn phoneNumberDtoIn);
    void deletePhoneNumber(UUID id);
}
