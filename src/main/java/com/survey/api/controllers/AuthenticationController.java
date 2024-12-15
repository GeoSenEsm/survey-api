package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;
import com.survey.application.services.AuthenticationService;
import com.survey.application.services.ClaimsPrincipalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, ClaimsPrincipalService claimsPrincipalService){
        this.authenticationService = authenticationService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping("/login")
    public String loginForRespondents(@Validated @RequestBody LoginDto loginDto){
        return authenticationService.getJwtTokenAsRespondent(loginDto);
    }

    @PostMapping("/login/admin")
    public String loginForAdmin(@Validated @RequestBody LoginDto loginDto){
        return authenticationService.getJwtTokenAsAdmin(loginDto);
    }

    @PostMapping("/respondents")
    @CrossOrigin
    public List<LoginDto> createRespondentsAccounts(@Validated @RequestBody CreateRespondentsAccountsDto dto){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return authenticationService.createRespondentsAccounts(dto);
    }
}
