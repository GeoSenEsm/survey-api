package com.survey.application.services;


import com.survey.application.dtos.CreateRespondentDataDto;

import javax.management.InvalidAttributeValueException;

public interface ForeignKeyValidationService {
    void validateForeignKeys(CreateRespondentDataDto dto) throws InvalidAttributeValueException;
}
