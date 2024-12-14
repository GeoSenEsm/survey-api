package com.survey.application.services;

import com.survey.application.dtos.RespondentGroupDto;

import java.util.List;
import java.util.UUID;

public interface RespondentGroupService {
    List<RespondentGroupDto> getRespondentGroups(UUID identityUserId);
}
