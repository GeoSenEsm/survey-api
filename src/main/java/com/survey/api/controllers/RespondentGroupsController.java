package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.RespondentGroupDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.RespondentGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/respondentgroups")
@Tag(name = "Respondent groups", description = "Endpoints for managing respondent groups.")
public class RespondentGroupsController {
    private final RespondentGroupService respondentGroupService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public RespondentGroupsController(RespondentGroupService respondentGroupService, ClaimsPrincipalService claimsPrincipalService){
        this.respondentGroupService = respondentGroupService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @GetMapping
    @Operation(
            summary = "Fetch all respondent groups / Fetch groups for given respondent.",
            description = """
                    - Optional parameter respondentId is passed:
                        - Groups that given respondent belongs to will be returned.
                    - Optional parameter respondentId is **not** passed:
                        - All existing respondent groups will be returned.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Respondent groups fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RespondentGroupDto.class))
                    )
            )
    })
    @CommonApiResponse403
    @CommonApiResponse400
    @CommonApiResponse401
    public ResponseEntity<List<RespondentGroupDto>> getRespondentGroups(@Validated @RequestParam(name = "respondentId", required = false) UUID identityUserId) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        List<RespondentGroupDto> respondentGroupDtos = respondentGroupService.getRespondentGroups(identityUserId);
        return ResponseEntity.ok(respondentGroupDtos);

    }

}
