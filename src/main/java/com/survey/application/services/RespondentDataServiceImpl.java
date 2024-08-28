package com.survey.application.services;

import com.survey.api.handlers.GlobalExceptionHandler;
import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.dtos.RespondentDataDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentData;
import com.survey.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import com.survey.domain.models.enums.Gender;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import java.util.Optional;
import java.util.UUID;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequestScope
public class RespondentDataServiceImpl implements RespondentDataService{
    private final RespondentDataRepository respondentDataRepository;
    private final ForeignKeyValidationServiceImpl foreignKeyValidationServiceImpl;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;
    private final IdentityUserRepository identityUserRepository;


    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository, ForeignKeyValidationServiceImpl foreignKeyValidationServiceImpl,
                                     ClaimsPrincipalService claimsPrincipalService, ModelMapper modelMapper,
                                     IdentityUserRepository identityUserRepository , EntityManager entityManager) {
        this.respondentDataRepository = respondentDataRepository;
        this.foreignKeyValidationServiceImpl = foreignKeyValidationServiceImpl;
        this.claimsPrincipalService = claimsPrincipalService;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
        this.identityUserRepository = identityUserRepository;
    }


    private UUID getUserUUID(String username){
        Optional<IdentityUser> optionalUser = identityUserRepository.findByUsername(username);
        return optionalUser.map(IdentityUser::getId).orElse(null);
    }

    private boolean doesRespondentDataExist(UUID userId) {
        return respondentDataRepository.existsByIdentityUserId(userId);
    }


    @Override
    @Transactional
    public RespondentDataDto createRespondent(CreateRespondentDataDto dto, String tokenWithPrefix)
            throws BadCredentialsException, InvalidAttributeValueException, InstanceAlreadyExistsException {

        String usernameFromJwt = claimsPrincipalService.getCurrentUsernameIfExists();

        UUID currentUserUUID = getUserUUID(usernameFromJwt);

        if (doesRespondentDataExist(currentUserUUID)) {
            throw new InstanceAlreadyExistsException("Respondent data already exists for this user.");
        }

        foreignKeyValidationServiceImpl.validateForeignKeys(dto);

        RespondentData respondentData = modelMapper.map(dto, RespondentData.class);
        respondentData.setIdentityUserId(currentUserUUID);
        respondentData.setGender(Gender.valueOf(dto.getGender()));

        RespondentData savedRespondentData = respondentDataRepository.save(respondentData);
        RespondentDataDto respondentDataDto = modelMapper.map(savedRespondentData, RespondentDataDto.class);

        return respondentDataDto;
    }

    @Override
    @Transactional
    public List<RespondentDataDto> getAll(){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RespondentData> cq = cb.createQuery(RespondentData.class);
        Root<RespondentData> respondentData = cq.from(RespondentData.class);
        Fetch<RespondentData, IdentityUser> identityUserFetch = respondentData.fetch("identityUser");
        cq.select(respondentData);

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(x -> modelMapper.map(x, RespondentDataDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RespondentDataDto getFromUserContext(){
        UUID currentUserId = claimsPrincipalService.findIdentityUser().getId();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RespondentData> cq = cb.createQuery(RespondentData.class);
        Root<RespondentData> respondentData = cq.from(RespondentData.class);
        Fetch<RespondentData, IdentityUser> identityUserFetch = respondentData.fetch("identityUser");
        Predicate condition = cb.equal(respondentData.get("identityUserId"), currentUserId);
        cq.where(condition);
        cq.select(respondentData);

        RespondentData dbRespondentData = entityManager
                .createQuery(cq)
                .getResultStream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        return modelMapper.map(dbRespondentData, RespondentDataDto.class);
    }
}