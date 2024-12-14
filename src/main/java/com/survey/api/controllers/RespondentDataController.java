package com.survey.api.controllers;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.services.RespondentDataService;
import com.survey.domain.models.enums.RespondentFilterOption;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/respondents")
public class RespondentDataController {

    private final RespondentDataService respondentDataService;

    @Autowired
    public RespondentDataController(RespondentDataService respondentDataService){
            this.respondentDataService = respondentDataService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> createRespondent(@Validated @RequestBody List<CreateRespondentDataDto> dto) throws BadRequestException, InvalidAttributeValueException, InstanceAlreadyExistsException {
        Map<String, Object> response = respondentDataService.createRespondent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(value = "filterOption", required = false) RespondentFilterOption filterOption,
            @RequestParam(value = "amount", required = false) Integer amount,
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") OffsetDateTime to
    ){
        List<Map<String, Object>> response = respondentDataService.getAll(filterOption, amount, from, to);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getFromUserContext(){
        Map<String, Object> response = respondentDataService.getFromUserContext();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateRespondent(@Validated @RequestBody List<CreateRespondentDataDto> dto,
                                                                    @RequestParam("respondentId") UUID identityUserId){
        Map<String, Object> response = respondentDataService.updateRespondent(dto, identityUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
