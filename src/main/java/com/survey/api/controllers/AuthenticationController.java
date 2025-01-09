package com.survey.api.controllers;

import com.survey.api.security.Role;
import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;
import com.survey.application.dtos.ChangePasswordDto;
import com.survey.application.services.AuthenticationService;
import com.survey.application.services.ClaimsPrincipalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/authentication")
@Tag(name = "Authentication", description = "Endpoints for managing authentication and user accounts.")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, ClaimsPrincipalService claimsPrincipalService){
        this.authenticationService = authenticationService;
        this.claimsPrincipalService = claimsPrincipalService;
    }



    @PostMapping("/login")
    @Operation(
            summary = "Login for respondents.",
            description = """
                    - Authenticate respondent and retrieve a JWT token.
                    - **Access:**
                        - unrestricted
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful. JWT token returned.")
    })
    public ResponseEntity<String> loginForRespondents(@Validated @RequestBody LoginDto loginDto){
        String jwtToken = authenticationService.getJwtTokenAsRespondent(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
    }


    @PostMapping("/login/admin")
    @Operation(
            summary = "Login for administrators.",
            description = """
                    - Authenticate an administrator and retrieve a JWT token.
                    - **Access:**
                        - unrestricted
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful. JWT token returned.")
    })
    public ResponseEntity<String> loginForAdmin(@Validated @RequestBody LoginDto loginDto){
        String jwtToken = authenticationService.getJwtTokenAsAdmin(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
    }


    @PostMapping("/respondents")
    @Operation(
            summary = "Create respondent accounts.",
            description = """
                    - Allows an admin to create accounts for respondents. Returns a list with usernames and passwords.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Respondent accounts created successfully. List of generated user credentials is returned.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoginDto.class))
                    ))
    })
    public ResponseEntity<List<LoginDto>> createRespondentsAccounts(@Validated @RequestBody CreateRespondentsAccountsDto dto){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<LoginDto> responseDtoList = authenticationService.createRespondentsAccounts(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtoList);
    }


    @PatchMapping("/password")
    @Operation(
            summary = "Change users own password.",
            description = """
                    - Allows respondents and administrators to change their own password.
                    - Correct current password must be provided in `ChangePasswordDto`.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully.")
    })
    public ResponseEntity<Void> updateOwnPassword(@Validated @RequestBody ChangePasswordDto changePasswordDto){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        authenticationService.updateOwnPassword(changePasswordDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PatchMapping("/admin/{respondentId}/password")
    @Operation(
            summary = "Change respondent password.",
            description = """
                    - Allows admin to update given respondents password.
                    - Old password is not required.
                    - To be used when respondent forgets their password.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respondent password updated successfully.")
    })
    public ResponseEntity<Void> updateUserPassword(@PathVariable("respondentId") UUID identityUserId, @Validated @RequestBody ChangePasswordDto changePasswordDto){
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        authenticationService.updateUserPassword(identityUserId, changePasswordDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
