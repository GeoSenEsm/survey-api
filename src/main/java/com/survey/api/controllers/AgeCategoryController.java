package com.survey.api.controllers;

import com.survey.application.dtos.AgeCategoryDto;
import com.survey.application.services.AgeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agecategories")
public class AgeCategoryController {
    private final AgeCategoryService ageCategoryService;

    @Autowired
    public AgeCategoryController(AgeCategoryService ageCategoryService){
        this.ageCategoryService = ageCategoryService;
    }

    @GetMapping
    public List<AgeCategoryDto> getAllAgeCategories(){
        return ageCategoryService.getAllAgeCategories();
    }
}
