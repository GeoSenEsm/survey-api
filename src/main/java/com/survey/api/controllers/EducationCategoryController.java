package com.survey.api.controllers;

import com.survey.application.dtos.EducationCategoryDto;
import com.survey.application.services.EducationCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/educationcategories")
public class EducationCategoryController {
    private final EducationCategoryService service;

    public EducationCategoryController(EducationCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<EducationCategoryDto> getAll() {
        return service.getAllEducationCategories();
    }
}
