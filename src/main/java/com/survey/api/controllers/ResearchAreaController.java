package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.ResearchAreaService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/researcharea")
public class ResearchAreaController {
    private final ResearchAreaService researchAreaService;
    private final ClaimsPrincipalService claimsPrincipalService;

    public ResearchAreaController(ResearchAreaService researchAreaService, ClaimsPrincipalService claimsPrincipalService) {
        this.researchAreaService = researchAreaService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    public ResponseEntity<List<ResponseResearchAreaDto>> saveResearchAreaData(@Valid @RequestBody List<ResearchAreaDto> researchAreaDtoList) throws BadRequestException {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseResearchAreaDto> responseResearchAreaDto = researchAreaService.saveResearchArea(researchAreaDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseResearchAreaDto);
    }

    @GetMapping
    public ResponseEntity<List<ResponseResearchAreaDto>> getResearchArea() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseResearchAreaDto> responseResearchAreaDtoList = researchAreaService.getResearchArea();
        if (responseResearchAreaDtoList != null) {
            return ResponseEntity.ok(responseResearchAreaDtoList);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResearchArea() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        boolean isDeleted = researchAreaService.deleteResearchArea();
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


}
