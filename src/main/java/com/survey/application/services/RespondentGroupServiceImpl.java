package com.survey.application.services;

import com.survey.application.dtos.RespondentGroupDto;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.RespondentDataRepository;
import com.survey.domain.repository.RespondentGroupRepository;
import com.survey.domain.repository.RespondentToGroupRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RespondentGroupServiceImpl implements RespondentGroupService {
    private final RespondentGroupRepository respondentGroupRepository;
    private final RespondentToGroupRepository respondentToGroupRepository;
    private final RespondentDataRepository respondentDataRepository;
    private final IdentityUserRepository identityUserRepository;
    private final ModelMapper modelMapper;

    public RespondentGroupServiceImpl(RespondentGroupRepository respondentGroupRepository, RespondentToGroupRepository respondentToGroupRepository, RespondentDataRepository respondentDataRepository, IdentityUserRepository identityUserRepository, ModelMapper modelMapper) {
        this.respondentGroupRepository = respondentGroupRepository;
        this.respondentToGroupRepository = respondentToGroupRepository;
        this.respondentDataRepository = respondentDataRepository;
        this.identityUserRepository = identityUserRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<RespondentGroupDto> getRespondentGroups(UUID identityUserId) {
        if (identityUserId != null) {
            if (!identityUserRepository.existsById(identityUserId)){
                throw new IllegalArgumentException("Respondent with given identity user ID does not exist.");
            }
            if(!respondentDataRepository.existsByIdentityUserId(identityUserId)){
                throw new IllegalArgumentException("Respondent with the provided identity user ID exists but probably did not fill the initial survey yet.");
            }
            return respondentToGroupRepository.findGroupsByIdentityUserId(identityUserId)
                    .stream()
                    .map(group -> modelMapper.map(group.getRespondentGroup(), RespondentGroupDto.class))
                    .collect(Collectors.toList());
        } else {
            return respondentGroupRepository.findAll()
                    .stream()
                    .map(group -> modelMapper.map(group, RespondentGroupDto.class))
                    .collect(Collectors.toList());
        }
    }
}
