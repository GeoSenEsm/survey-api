package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.repository.*;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.List;


@Component
public class ForeignKeyValidationServiceImpl implements ForeignKeyValidationService{

    private final AgeCategoryRepository ageCategoryRepository;
    private final OccupationCategoryRepository occupationCategoryRepository;
    private final EducationCategoryRepository educationCategoryRepository;
    private final HealthConditionRepository healthConditionRepository;
    private final MedicationUseRepository medicationUseRepository;
    private final LifeSatisfactionRepository lifeSatisfactionRepository;
    private final StressLevelRepository stressLevelRepository;
    private final QualityOfSleepRepository qualityOfSleepRepository;
    private final GreeneryAreaCategoryRepository greeneryAreaCategoryRepository;

    @Autowired
    public ForeignKeyValidationServiceImpl(
            AgeCategoryRepository ageCategoryRepository,
            OccupationCategoryRepository occupationCategoryRepository,
            EducationCategoryRepository educationCategoryRepository,
            HealthConditionRepository healthConditionRepository,
            MedicationUseRepository medicationUseRepository,
            LifeSatisfactionRepository lifeSatisfactionRepository,
            StressLevelRepository stressLevelRepository,
            QualityOfSleepRepository qualityOfSleepRepository,
            GreeneryAreaCategoryRepository greeneryAreaCategoryRepository
    ) {
        this.ageCategoryRepository = ageCategoryRepository;
        this.occupationCategoryRepository = occupationCategoryRepository;
        this.educationCategoryRepository = educationCategoryRepository;
        this.healthConditionRepository = healthConditionRepository;
        this.medicationUseRepository = medicationUseRepository;
        this.lifeSatisfactionRepository = lifeSatisfactionRepository;
        this.stressLevelRepository = stressLevelRepository;
        this.qualityOfSleepRepository = qualityOfSleepRepository;
        this.greeneryAreaCategoryRepository = greeneryAreaCategoryRepository;
    }

    public void validateForeignKeys(CreateRespondentDataDto dto) throws InvalidAttributeValueException {
        List<String> validationErrors = new ArrayList<>();
        collectValidationError("ageCategory", dto.getAgeCategoryId(), ageCategoryRepository, validationErrors);
        collectValidationError("occupationCategory", dto.getOccupationCategoryId(), occupationCategoryRepository, validationErrors);
        collectValidationError("educationCategory", dto.getEducationCategoryId(), educationCategoryRepository, validationErrors);
        collectValidationError("healthCondition", dto.getHealthConditionId(), healthConditionRepository, validationErrors);
        collectValidationError("medicationUse", dto.getMedicationUseId(), medicationUseRepository, validationErrors);
        collectValidationError("lifeSatisfaction", dto.getLifeSatisfactionId(), lifeSatisfactionRepository, validationErrors);
        collectValidationError("stressLevel", dto.getStressLevelId(), stressLevelRepository, validationErrors);
        collectValidationError("qualityOfSleep", dto.getQualityOfSleepId(), qualityOfSleepRepository, validationErrors);
        collectValidationError("greeneryAreaCategory", dto.getGreeneryAreaCategoryId(), greeneryAreaCategoryRepository, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new InvalidAttributeValueException(String.join("\n", validationErrors));
        }
    }

    private void collectValidationError(String fieldName, Integer id, JpaRepository<?, Integer> repository, List<String> validationErrors) {
        if (id == null || !repository.existsById(id)) {
            validationErrors.add("Invalid foreign key id for field: " + fieldName);
        }
    }
}
