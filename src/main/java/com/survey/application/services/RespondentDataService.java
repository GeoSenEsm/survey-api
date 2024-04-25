package com.survey.application.services;

import com.survey.application.dtos.RespondentDataDto;
import org.springframework.http.ResponseEntity;

public interface RespondentDataService {
    ResponseEntity<String> createRespondent(RespondentDataDto dto, String token);
}
