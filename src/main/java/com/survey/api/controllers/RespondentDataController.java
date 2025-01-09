package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.RespondentDataService;
import com.survey.domain.models.enums.RespondentFilterOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Respondent data", description = "Endpoints for managing respondent data and answering initial survey.")
public class RespondentDataController {

    private final RespondentDataService respondentDataService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public RespondentDataController(RespondentDataService respondentDataService, ClaimsPrincipalService claimsPrincipalService){
            this.respondentDataService = respondentDataService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    @Operation(
            summary = "Send initial survey response.",
            description = """
                    - Endpoint for respondents to send their answers to initial survey.
                    - After sending the response, respondent is assigned to correct groups.
                    - Body dto contains `questionId` from initial survey and corresponding `optionId` from this question that the respondent selected.
                    - **Access:**
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Respondent assigned to appropriate groups successfully.",
                    content = @Content(
                            mediaType = "application/json"
                    ))
    })
    public ResponseEntity<Map<String, Object>> createRespondent(@Validated @RequestBody List<CreateRespondentDataDto> dto) throws BadRequestException, InvalidAttributeValueException, InstanceAlreadyExistsException {
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
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
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<Map<String, Object>> response = respondentDataService.getAll(filterOption, amount, from, to);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFromUserContext(){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        Map<String, Object> response = respondentDataService.getFromUserContext();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateRespondent(@Validated @RequestBody List<CreateRespondentDataDto> dto,
                                                                    @RequestParam("respondentId") UUID identityUserId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        Map<String, Object> response = respondentDataService.updateRespondent(dto, identityUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
