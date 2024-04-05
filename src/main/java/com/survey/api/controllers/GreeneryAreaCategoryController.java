package com.survey.api.controllers;

import com.survey.application.dtos.GreeneryAreaCategoryDto;
import com.survey.application.services.GreeneryAreaCategoryService;
import com.survey.domain.models.GreeneryAreaCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/greeneryareacategories")
public class GreeneryAreaCategoryController {
    private final GreeneryAreaCategoryService greeneryAreaCategoryService;

    @Autowired
    public GreeneryAreaCategoryController(GreeneryAreaCategoryService greeneryAreaCategoryService){
        this.greeneryAreaCategoryService = greeneryAreaCategoryService;
    }

    @GetMapping
    public List<GreeneryAreaCategoryDto> getAllGreeneryAreaCategories() {
        return greeneryAreaCategoryService.getAllGreeneryAreaCategories();
    }

}


