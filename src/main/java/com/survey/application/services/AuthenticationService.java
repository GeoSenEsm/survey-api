package com.survey.application.services;

import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;

import java.util.List;


public interface AuthenticationService {
    String getJwtTokenAsRespondent(LoginDto dto);
    String getJwtTokenAsAdmin(LoginDto dto);
    List<LoginDto> createRespondentsAccounts(CreateRespondentsAccountsDto dto);
}
