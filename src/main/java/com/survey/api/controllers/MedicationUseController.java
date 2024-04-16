package com.survey.api.controllers;

import com.survey.application.dtos.MedicationUseDto;
import com.survey.application.services.MedicationUseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicationuse")
public class MedicationUseController {
    private final MedicationUseService service;

    @Autowired
    public MedicationUseController(MedicationUseService service){
        this.service = service;
    }

    @GetMapping
    public List<MedicationUseDto> getAllGreeneryAreaCategories() {
        return service.getMedicationUse();
    }
}
