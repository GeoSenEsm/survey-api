package com.survey.application.services;

import com.survey.api.handlers.GlobalExceptionHandler;
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
    private final ClaimsPrincipalServiceImpl claimsPrincipalServiceImpl;
    @Autowired
    private IdentityUserRepository identityUserRepository;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    public RespondentDataServiceImpl(RespondentDataRepository respondentDataRepository, ForeignKeyValidationServiceImpl foreignKeyValidationServiceImpl, ClaimsPrincipalServiceImpl claimsPrincipalServiceImpl, ModelMapper modelMapper) {
        this.respondentDataRepository = respondentDataRepository;
        this.foreignKeyValidationServiceImpl = foreignKeyValidationServiceImpl;
        this.claimsPrincipalServiceImpl = claimsPrincipalServiceImpl;
        this.modelMapper = modelMapper;

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

        String usernameFromJwt = claimsPrincipalServiceImpl.getCurrentUsernameIfExists();

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
}