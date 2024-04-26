package com.survey.application.services;

import com.survey.api.handlers.GlobalExceptionHandler;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.dtos.RespondentDataDto;
import com.survey.domain.models.Gender;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentData;
import com.survey.domain.repository.*;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RespondentDataServiceImpl implements RespondentDataService{
    private final RespondentDataRepository respondentDataRepository;
    private final Map<String, JpaRepository<?, Integer>> repositoryMap;
    @Autowired
    private IdentityUserRepository identityUserRepository;
    private final TokenProvider tokenProvider;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository, AgeCategoryRepository ageCategoryRepository, OccupationCategoryRepository occupationCategoryRepository, EducationCategoryRepository educationCategoryRepository, HealthConditionRepository healthConditionRepository, MedicationUseRepository medicationUseRepository, LifeSatisfactionRepository lifeSatisfactionRepository, StressLevelRepository stressLevelRepository, QualityOfSleepRepository qualityOfSleepRepository, GreeneryAreaCategoryRepository greeneryAreaCategoryRepository, TokenProvider tokenProvider, ModelMapper modelMapper) {
        this.respondentDataRepository = respondentDataRepository;
        this.tokenProvider = tokenProvider;
        this.modelMapper = modelMapper;
        this.repositoryMap = new HashMap<>();
        repositoryMap.put("ageCategory", ageCategoryRepository);
        repositoryMap.put("occupationCategory", occupationCategoryRepository);
        repositoryMap.put("educationCategory", educationCategoryRepository);
        repositoryMap.put("healthCondition", healthConditionRepository);
        repositoryMap.put("medicationUse", medicationUseRepository);
        repositoryMap.put("lifeSatisfaction", lifeSatisfactionRepository);
        repositoryMap.put("stressLevel", stressLevelRepository);
        repositoryMap.put("qualityOfSleep", qualityOfSleepRepository);
        repositoryMap.put("greeneryAreaCategory", greeneryAreaCategoryRepository);
    }

    private Integer getIdByFieldName(CreateRespondentDataDto dto, String fieldName) {
        return switch (fieldName) {
            case "ageCategory" -> dto.getAgeCategoryId();
            case "occupationCategory" -> dto.getOccupationCategoryId();
            case "educationCategory" -> dto.getEducationCategoryId();
            case "healthCondition" -> dto.getHealthConditionId();
            case "medicationUse" -> dto.getMedicationUseId();
            case "lifeSatisfaction" -> dto.getLifeSatisfactionId();
            case "stressLevel" -> dto.getStressLevelId();
            case "qualityOfSleep" -> dto.getQualityOfSleepId();
            case "greeneryAreaCategory" -> dto.getGreeneryAreaCategoryId();
            default -> null;
        };
    }

    private UUID getUserUUID(String username){
        Optional<IdentityUser> optionalUser = identityUserRepository.findByUsername(username);
        return optionalUser.map(IdentityUser::getId).orElse(null);
    }

    private boolean doesRespondentDataExist(UUID userId) {
        return respondentDataRepository.existsByIdentityUserId(userId);
    }


    @Override
    public RespondentDataDto createRespondent(CreateRespondentDataDto dto, String token) throws BadRequestException {
        if (token == null){
            throw new BadCredentialsException("Token is missing in headers.");
        }

        tokenProvider.validateToken(token);

        String usernameFromJwt = tokenProvider.getUsernameFromJwt(token);
        UUID currentUserUUID = getUserUUID(usernameFromJwt);

        if (doesRespondentDataExist(currentUserUUID)) {
            throw new BadRequestException("Respondent data already exists for this user.");
        }


        // foreign keys validation
        for (String fieldName: repositoryMap.keySet()){
            Integer id = getIdByFieldName(dto, fieldName);
            JpaRepository<?, Integer> repository = repositoryMap.get(fieldName);
            if (id == null || !repository.existsById(id)){
                throw new BadRequestException("Invalid foreign key id.");
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
        respondentData.setGreeneryAreaCategoryId(dto.getGreeneryAreaCategoryId());

/*        RespondentData respondentData = modelMapper.map(dto, RespondentData.class);
        respondentData.setIdentityUserId(currentUserUUID);*/

        RespondentData savedRespondentData = respondentDataRepository.save(respondentData);

        RespondentDataDto respondentDataDto = modelMapper.map(savedRespondentData, RespondentDataDto.class);

        return respondentDataDto;
    }
}