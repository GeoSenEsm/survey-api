package com.survey.application.services;

import com.survey.application.dtos.RespondentDataDto;
import com.survey.domain.models.Gender;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentData;
import com.survey.domain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RespondentDataServiceImpl implements RespondentDataService{
    private final RespondentDataRepository respondentDataRepository;
    private final Map<String, JpaRepository<?, Integer>> repositoryMap;
    @Autowired
    private IdentityUserRepository identityUserRepository;

    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository, AgeCategoryRepository ageCategoryRepository, OccupationCategoryRepository occupationCategoryRepository, EducationCategoryRepository educationCategoryRepository, HealthConditionRepository healthConditionRepository, MedicationUseRepository medicationUseRepository, LifeSatisfactionRepository lifeSatisfactionRepository, StressLevelRepository stressLevelRepository, QualityOfSleepRepository qualityOfSleepRepository) {
        this.respondentDataRepository = respondentDataRepository;
        this.repositoryMap = new HashMap<>();
        repositoryMap.put("ageCategory", ageCategoryRepository);
        repositoryMap.put("occupationCategory", occupationCategoryRepository);
        repositoryMap.put("educationCategory", educationCategoryRepository);
        repositoryMap.put("healthCondition", healthConditionRepository);
        repositoryMap.put("medicationUse", medicationUseRepository);
        repositoryMap.put("lifeSatisfaction", lifeSatisfactionRepository);
        repositoryMap.put("stressLevel", stressLevelRepository);
        repositoryMap.put("qualityOfSleep", qualityOfSleepRepository);
    }

    private Integer getIdByFieldName(RespondentDataDto dto, String fieldName) {
        return switch (fieldName) {
            case "ageCategory" -> dto.getAgeCategoryId();
            case "occupationCategory" -> dto.getOccupationCategoryId();
            case "educationCategory" -> dto.getEducationCategoryId();
            case "healthCondition" -> dto.getHealthConditionId();
            case "medicationUse" -> dto.getMedicationUseId();
            case "lifeSatisfaction" -> dto.getLifeSatisfactionId();
            case "stressLevel" -> dto.getStressLevelId();
            case "qualityOfSleep" -> dto.getQualityOfSleepId();
            default -> null;
        };
    }

    private boolean isValidGender(String gender) {
        return gender.equals("male") || gender.equals("female");
    }

    private UUID getUserUUID(String username){
        Optional<IdentityUser> optionalUser = identityUserRepository.findByUsername(username);
        return optionalUser.map(IdentityUser::getId).orElse(null);
    }

    private boolean doesRespondentDataExist(UUID userId) {
        return respondentDataRepository.existsByIdentityUserId(userId);
    }


    @Override
    public ResponseEntity<String> createRespondent(RespondentDataDto dto) {
        String currentUserUsername = dto.getUsername();
        UUID currentUserUUID = getUserUUID(currentUserUsername);

        if (currentUserUUID == null){
            return ResponseEntity.badRequest().body("No user with username: " + currentUserUsername);
        }

        if (doesRespondentDataExist(currentUserUUID)) {
            return ResponseEntity.badRequest().body("Respondent data record already exists for this user.");
        }

        if (dto.getGender() == null || !isValidGender(dto.getGender())) {
            return ResponseEntity.badRequest().body("Invalid or missing gender.");
        }

        // foreign keys validation
        for (String fieldName: repositoryMap.keySet()){
            Integer id = getIdByFieldName(dto, fieldName);
            JpaRepository<?, Integer> repository = repositoryMap.get(fieldName);
            if (id == null || !repository.existsById(id)){
                return ResponseEntity.badRequest().body("Invalid foreign key ID.");
            }
        }

        RespondentData respondentData = new RespondentData();
        respondentData.setIdentityUserId(currentUserUUID);
        respondentData.setGender(Gender.valueOf(dto.getGender()).getId());
        respondentData.setAgeCategoryId(dto.getAgeCategoryId());
        respondentData.setOccupationCategoryId(dto.getOccupationCategoryId());
        respondentData.setEducationCategoryId(dto.getEducationCategoryId());
        respondentData.setHealthConditionId(dto.getHealthConditionId());
        respondentData.setMedicationUseId(dto.getMedicationUseId());
        respondentData.setLifeSatisfactionId(dto.getLifeSatisfactionId());
        respondentData.setStressLevelId(dto.getStressLevelId());
        respondentData.setQualityOfSleepId(dto.getQualityOfSleepId());
        respondentDataRepository.save(respondentData);

        return ResponseEntity.status(HttpStatus.CREATED).body("Respondent data created successfully.");
    }
}