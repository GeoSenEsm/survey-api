package com.survey.api.controllers;

import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;
import com.survey.application.services.ResearchAreaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/researcharea")
public class ResearchAreaController {
    private final ResearchAreaService researchAreaService;

    public ResearchAreaController(ResearchAreaService researchAreaService) {
        this.researchAreaService = researchAreaService;
    }

    @PostMapping
    public ResponseEntity<ResponseResearchAreaDto> saveResearchAreaData(@Valid @RequestBody ResearchAreaDto researchAreaDto) {
        ResponseResearchAreaDto responseResearchAreaDto = researchAreaService.saveResearchArea(researchAreaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseResearchAreaDto);
    }

    @GetMapping
    public ResponseEntity<ResponseResearchAreaDto> getResearchArea() {
        ResponseResearchAreaDto responseResearchAreaDto = researchAreaService.getResearchArea();
        if (responseResearchAreaDto != null) {
            return ResponseEntity.ok(responseResearchAreaDto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResearchArea() {
        boolean isDeleted = researchAreaService.deleteResearchArea();
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


}
