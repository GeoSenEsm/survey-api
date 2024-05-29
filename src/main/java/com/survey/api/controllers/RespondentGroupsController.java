package com.survey.api.controllers;

import com.survey.application.dtos.RespondentGroupDto;
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
    @Autowired
    public RespondentGroupsController(RespondentGroupService respondentGroupService){
        this.respondentGroupService = respondentGroupService;
    }
    @GetMapping
    public ResponseEntity<List<RespondentGroupDto>> getRespondentGroups(@Validated @RequestParam(name = "respondentId", required = false) UUID respondentId) {
        List<RespondentGroupDto> respondentGroupDtos = respondentGroupService.getRespondentGroups(respondentId);
        return ResponseEntity.ok(respondentGroupDtos);

    }

}
