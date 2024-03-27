package com.survey.application.services;

import com.survey.domain.models.GreeneryAreaCategory;
import com.survey.domain.repository.GreeneryAreaCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class GreeneryAreaCategoryServiceImpl implements GreeneryAreaCategoryService{
    @Autowired
    private GreeneryAreaCategoryRepository repository;

    @Override
    public List<GreeneryAreaCategory> getAllGreeneryAreaCategories() {
        return repository.findAll();
    }
}
