package com.survey.application.services;

import com.survey.application.dtos.RespondentGroupDto;
import com.survey.domain.repository.RespondentDataRepository;
import com.survey.domain.repository.RespondentGroupRepository;
import com.survey.domain.repository.RespondentToGroupRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RespondentGroupServiceImpl implements RespondentGroupService {
    private final RespondentGroupRepository respondentGroupRepository;
    private final RespondentToGroupRepository respondentToGroupRepository;
    private final RespondentDataRepository respondentDataRepository;
    private final ModelMapper modelMapper;
    private final SessionContext sessionContext;

    public RespondentGroupServiceImpl(RespondentGroupRepository respondentGroupRepository, RespondentToGroupRepository respondentToGroupRepository, RespondentDataRepository respondentDataRepository, ModelMapper modelMapper, SessionContext sessionContext) {
        this.respondentGroupRepository = respondentGroupRepository;
        this.respondentToGroupRepository = respondentToGroupRepository;
        this.respondentDataRepository = respondentDataRepository;
        this.modelMapper = modelMapper;
        this.sessionContext = sessionContext;
    }

    @Override
    public List<RespondentGroupDto> getRespondentGroups(UUID respondentId) {
        String lang = sessionContext.getClientLang();
        if (respondentId != null) {
            if(respondentDataRepository.existsByIdentityUserId(respondentId)){
                throw new IllegalArgumentException("Invalid respondent ID - respondent doesn't exist");
            }
            return respondentToGroupRepository.findGroupsByRespondentDataId(respondentId)
                    .stream()
                    .map(group -> modelMapper.map(group.getRespondentGroup(), RespondentGroupDto.class)
                            .setName(lang != null && lang.equals("pl") ? group.getRespondentGroup().getPolishName() : group.getRespondentGroup().getEnglishName()))
                    .collect(Collectors.toList());
        } else {
            return respondentGroupRepository.findAll()
                    .stream()
                    .map(group -> modelMapper.map(group, RespondentGroupDto.class)
                            .setName(lang != null && lang.equals("pl") ? group.getPolishName() : group.getEnglishName()))
                    .collect(Collectors.toList());
        }

    }

}
