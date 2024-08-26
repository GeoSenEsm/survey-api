package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentDataDto;
import com.survey.application.dtos.RespondentDataDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.RespondentData;
import com.survey.domain.models.enums.Gender;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.RespondentDataRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
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
    private final IdentityUserRepository identityUserRepository;


    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository, ForeignKeyValidationServiceImpl foreignKeyValidationServiceImpl,
                                     ClaimsPrincipalService claimsPrincipalService, ModelMapper modelMapper,
                                     IdentityUserRepository identityUserRepository) {
        this.respondentDataRepository = respondentDataRepository;
        this.foreignKeyValidationServiceImpl = foreignKeyValidationServiceImpl;
        this.claimsPrincipalService = claimsPrincipalService;
        this.modelMapper = modelMapper;
        this.identityUserRepository = identityUserRepository;

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        TypeMap<CreateRespondentDataDto, RespondentData> typeMap =
                modelMapper.createTypeMap(CreateRespondentDataDto.class, RespondentData.class);
        typeMap.addMappings(mapper -> {
            mapper.skip(RespondentData::setId);
            mapper.skip(RespondentData::setIdentityUserId);
            mapper.skip(RespondentData::setGender);
        });
    }


    private UUID getUserUUID(String username){
        Optional<IdentityUser> optionalUser = identityUserRepository.findByUsername(username);
        return optionalUser.map(IdentityUser::getId).orElse(null);
    }

    private boolean doesRespondentDataExist(UUID userId) {
        return respondentDataRepository.existsByIdentityUserId(userId);
    }


    @Override
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
    public List<RespondentDataDto> getAll(){
        return respondentDataRepository
                .findAll()
                .stream()
                .map(x -> modelMapper.map(x, RespondentDataDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RespondentDataDto getFromUserContext(){
        UUID currentUserId = claimsPrincipalService.findIdentityUser().getId();
        RespondentData respondentData = respondentDataRepository.findByIdentityUserId(currentUserId);
        return modelMapper.map(respondentData, RespondentDataDto.class);
    }
}