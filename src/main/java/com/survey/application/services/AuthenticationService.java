package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;
import com.survey.application.dtos.surveyDtos.ChangePasswordDto;

import java.util.List;
import java.util.UUID;


public interface AuthenticationService {
    String getJwtTokenAsRespondent(LoginDto dto);
    String getJwtTokenAsAdmin(LoginDto dto);
    List<LoginDto> createRespondentsAccounts(CreateRespondentsAccountsDto dto);
    void updateUserPassword(UUID identityUserId, ChangePasswordDto changePasswordDto);
}
