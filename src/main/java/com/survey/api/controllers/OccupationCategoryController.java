package com.survey.api.controllers;

import com.survey.application.dtos.OccupationCategoryDto;
import com.survey.application.services.OccupationCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/occupationcategories")
public class OccupationCategoryController {
    private final OccupationCategoryService service;

    @Autowired
    public OccupationCategoryController(OccupationCategoryService service){
        this.service = service;
    }

    @GetMapping
    public List<OccupationCategoryDto> getAllGreeneryAreaCategories() {
        return service.getAllOccupationCategories();
    }
}
