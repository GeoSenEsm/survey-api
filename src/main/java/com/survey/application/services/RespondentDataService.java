package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.dtos.RespondentDataDto;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RespondentDataService {
    RespondentDataDto createRespondent(CreateRespondentDataDto dto, String token) throws BadRequestException;
}
