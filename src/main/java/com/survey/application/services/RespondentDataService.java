package com.survey.application.services;

import com.survey.application.dtos.RespondentDataDto;
import com.survey.domain.models.RespondentData;
import com.survey.domain.repository.RespondentDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.logging.Logger;

@Service
public class RespondentDataService {
    private final RespondentDataRepository respondentDataRepository;

    @Autowired
    public RespondentDataService(RespondentDataRepository respondentDataRepository) {
        this.respondentDataRepository = respondentDataRepository;
    }

    private boolean isValidGender(String gender) {
        return gender != null && (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"));
    }

    private boolean isValidCategoryId(Integer categoryId) {
        //PLACEHOLDER - implement laterr
        return true;
    }

    public ResponseEntity<String> createRespondent(RespondentDataDto dto) {
        Integer genderId = dto.getGenderId();

        RespondentData respondentData = new RespondentData();

        respondentData.setGenderId(dto.getGenderId());
        respondentData.setAgeCategoryId(dto.getAgeCategoryId());
        respondentData.setOccupationCategoryId(dto.getOccupationCategoryId());
        respondentData.setEducationCategoryId(dto.getEducationCategoryId());
        respondentData.setHealthConditionId(dto.getHealthConditionId());
        respondentData.setMedicationUseId(dto.getMedicationUseId());
        respondentData.setLifeSatisfactionId(dto.getLifeSatisfactionId());
        respondentData.setStressLevelId(dto.getStressLevelId());
        respondentData.setQualityOfSleepId(dto.getQualityOfSleepId());

        respondentDataRepository.save(respondentData);

        return ResponseEntity.status(HttpStatus.CREATED).body("Respondent created successfully. GenderId: " + genderId);
    }
}
