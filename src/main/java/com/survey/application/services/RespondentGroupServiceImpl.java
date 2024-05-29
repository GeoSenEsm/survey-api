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
    @Autowired
    private RespondentGroupRepository respondentGroupRepository;
    @Autowired
    private RespondentToGroupRepository respondentToGroupRepository;

    @Autowired
    private RespondentDataRepository respondentDataRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<RespondentGroupDto> getRespondentGroups(UUID respondentId) {

        if (respondentId != null) {
            if(respondentDataRepository.existsByIdentityUserId(respondentId)){
                throw new IllegalArgumentException("Invalid respondent ID - respondent doesn't exist");
            }
            return respondentToGroupRepository.findGroupsByRespondentDataId(respondentId)
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
