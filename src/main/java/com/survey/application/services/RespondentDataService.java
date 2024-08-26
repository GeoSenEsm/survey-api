package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.dtos.RespondentDataDto;
import org.springframework.security.authentication.BadCredentialsException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import java.util.List;

public interface RespondentDataService {
    RespondentDataDto createRespondent(CreateRespondentDataDto dto, String tokenBearerPrefix) throws BadCredentialsException, InvalidAttributeValueException, InstanceAlreadyExistsException;
    List<RespondentDataDto> getAll();

    RespondentDataDto getFromUserContext();
}
