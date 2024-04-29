package com.survey.application.services;


import com.survey.api.handlers.GlobalExceptionHandler;
import com.survey.application.dtos.CreateRespondentDataDto;
import org.apache.coyote.BadRequestException;

public interface ForeignKeyValidationService {
    void validateForeignKeys(CreateRespondentDataDto dto) throws BadRequestException;
}
