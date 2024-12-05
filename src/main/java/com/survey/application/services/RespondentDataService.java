package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.models.enums.RespondentFilterOption;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.BadCredentialsException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface RespondentDataService {

    Map<String, Object> createRespondent(List<CreateRespondentDataDto> dto, String tokenBearerPrefix) throws BadCredentialsException, InvalidAttributeValueException, InstanceAlreadyExistsException, BadRequestException;
    List<Map<String, Object>> getAll(RespondentFilterOption filterOption, Integer amount, OffsetDateTime from, OffsetDateTime to);
    Map<String, Object> getFromUserContext();
}
