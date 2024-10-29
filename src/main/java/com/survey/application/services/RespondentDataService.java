package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.BadCredentialsException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import java.util.List;
import java.util.Map;

public interface RespondentDataService {

    Map<String, Object> createRespondent(List<CreateRespondentDataDto> dto, String tokenBearerPrefix) throws BadCredentialsException, InvalidAttributeValueException, InstanceAlreadyExistsException, BadRequestException;
    List<Map<String, Object>> getAll();
    Map<String, Object> getFromUserContext();
}
