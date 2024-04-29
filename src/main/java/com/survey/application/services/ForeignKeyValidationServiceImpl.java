package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.domain.repository.*;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;


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

    public void validateForeignKeys(CreateRespondentDataDto dto) throws BadRequestException {
        validateForeignKey("ageCategory", dto.getAgeCategoryId(), ageCategoryRepository);
        validateForeignKey("occupationCategory", dto.getOccupationCategoryId(), occupationCategoryRepository);
        validateForeignKey("educationCategory", dto.getEducationCategoryId(), educationCategoryRepository);
        validateForeignKey("healthCondition", dto.getHealthConditionId(), healthConditionRepository);
        validateForeignKey("medicationUse", dto.getMedicationUseId(), medicationUseRepository);
        validateForeignKey("lifeSatisfaction", dto.getLifeSatisfactionId(), lifeSatisfactionRepository);
        validateForeignKey("stressLevel", dto.getStressLevelId(), stressLevelRepository);
        validateForeignKey("qualityOfSleep", dto.getQualityOfSleepId(), qualityOfSleepRepository);
        validateForeignKey("greeneryAreaCategory", dto.getGreeneryAreaCategoryId(), greeneryAreaCategoryRepository);
    }

    private void validateForeignKey(String fieldName, Integer id, JpaRepository<?, Integer> repository) throws BadRequestException {
        if (id == null || !repository.existsById(id)) {
            throw new BadRequestException("Invalid foreign key id for field: " + fieldName);
        }
    }
}
