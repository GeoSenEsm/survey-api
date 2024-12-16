package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.RespondentGroupDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.RespondentGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/respondentgroups")
public class RespondentGroupsController {
    private final RespondentGroupService respondentGroupService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public RespondentGroupsController(RespondentGroupService respondentGroupService, ClaimsPrincipalService claimsPrincipalService){
        this.respondentGroupService = respondentGroupService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @GetMapping
    public ResponseEntity<List<RespondentGroupDto>> getRespondentGroups(@Validated @RequestParam(name = "respondentId", required = false) UUID identityUserId) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        List<RespondentGroupDto> respondentGroupDtos = respondentGroupService.getRespondentGroups(identityUserId);
        return ResponseEntity.ok(respondentGroupDtos);

    }

}
