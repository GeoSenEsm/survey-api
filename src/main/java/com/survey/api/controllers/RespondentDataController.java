package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.RespondentDataService;
import com.survey.domain.models.enums.RespondentFilterOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Respondent data", description = "Endpoints for managing respondent data, answering initial survey, filtering respondents.")
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
            @ApiResponse(
                    responseCode = "201",
                    description = "Respondent data created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    description = """
                                            - Example response containing respondent details.
                                            - Response contains:
                                                - User UUID
                                                - User username
                                                - All groups that respondent belongs to in format
                                                    - "groupName": "group UUID"
                                            """,
                                    example = """
                                    {
                                        "id": "631037ab-b4fc-4de1-8794-3bdc64f98f66",
                                        "username": "00001",
                                        "Gender": "98472a71-6f0f-48fd-b702-a9c3fd508212",
                                        "Student": "f8e28b55-7113-4ccf-ad93-d5415739c2e6"
                                    }
                                    """
                            )
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Map<String, Object>> createRespondent(@Validated @RequestBody List<CreateRespondentDataDto> dto) throws BadRequestException, InvalidAttributeValueException, InstanceAlreadyExistsException {
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
        Map<String, Object> response = respondentDataService.createRespondent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/all")
    @Operation(
            summary = "Fetch and filter respondents.",
            description = """
                    - Allows admin to fetch information about respondents.
                    - **No filters selected:**
                        - All existing respondents returned.
                    - **Filters:**
                        - `skipped_surveys`
                            - Amount - ex. I want to select respondents that did not fill 5 or more surveys -> amount = 5
                            - Specify date range.
                        - `location_not_sent`
                            - Amount - ex. I want to select respondents that send geolocation data less than 5 times -> amount = 5
                            - Specify date range.
                        - `sensor_data_not_sent`
                            - Amount - ex. I want to select respondents that send temperature sensor data less than 5 times -> amount = 5
                            - Specify date range.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Respondent data fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    description = """
                                            - Example response containing a list of respondent details.
                                            - Response contains:
                                                - User UUID
                                                - User username
                                                - All groups that respondent belongs to in format
                                                    - "groupName": "group UUID"
                                            """,
                                    example = """
                                    [
                                        {
                                            "id": "631037ab-b4fc-4de1-8794-3bdc64f98f66",
                                            "username": "00001",
                                            "Gender": "98472a71-6f0f-48fd-b702-a9c3fd508212",
                                            "Student": "f8e28b55-7113-4ccf-ad93-d5415739c2e6"
                                        },
                                        {
                                            "id": "731037ab-b4fc-4de2-8794-3bdc64f98f99",
                                            "username": "00002"
                                        }
                                    ]
                                    """
                            )
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
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
    @Operation(
            summary = "Fetch respondent data.",
            description = """
                    - Allows respondent to fetch data about themselves.
                    - **Access:**
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Respondent data fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    description = """
                                            - Example response containing respondent details.
                                            - Response contains:
                                                - User UUID
                                                - User username
                                                - All groups that respondent belongs to in format
                                                    - "groupName": "group UUID"
                                            """,
                                    example = """
                                    {
                                        "id": "631037ab-b4fc-4de1-8794-3bdc64f98f66",
                                        "username": "00001",
                                        "Gender": "98472a71-6f0f-48fd-b702-a9c3fd508212",
                                        "Student": "f8e28b55-7113-4ccf-ad93-d5415739c2e6"
                                    }
                                    """
                            )
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Map<String, Object>> getFromUserContext(){
        claimsPrincipalService.ensureRole(Role.RESPONDENT.getRoleName());
        Map<String, Object> response = respondentDataService.getFromUserContext();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PutMapping
    @Operation(
            summary = "Update groups that respondent belongs to.",
            description = """
                    - Endpoint allows admin to update the respondent groups that given respondent belongs to.
                    - In other words it updated given respondent answers from initial survey.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Respondent data updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    description = """
                                            - Example response containing respondent details.
                                            - Response contains:
                                                - User UUID
                                                - User username
                                                - All groups that respondent belongs to in format
                                                    - "groupName": "group UUID"
                                            """,
                                    example = """
                                    {
                                        "id": "631037ab-b4fc-4de1-8794-3bdc64f98f66",
                                        "username": "00001",
                                        "Gender": "98472a71-6f0f-48fd-b702-a9c3fd508212",
                                        "Student": "f8e28b55-7113-4ccf-ad93-d5415739c2e6"
                                    }
                                    """
                            )
                    )
            )
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Map<String, Object>> updateRespondent(@Validated @RequestBody List<CreateRespondentDataDto> dto,
                                                                    @RequestParam("respondentId") UUID identityUserId){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        Map<String, Object> response = respondentDataService.updateRespondent(dto, identityUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
