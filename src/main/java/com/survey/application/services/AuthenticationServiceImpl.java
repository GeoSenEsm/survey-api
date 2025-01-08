package com.survey.application.services;

import com.survey.api.security.Role;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;
import com.survey.application.dtos.ChangePasswordDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final IdentityUserRepository identityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialsGenerator credentialsGenerator;
    private final PasswordValidationService passwordValidationService;
    private final ClaimsPrincipalService claimsPrincipalService;


    @Autowired
    public AuthenticationServiceImpl(AuthenticationManager authenticationManager, TokenProvider tokenProvider,
                                     IdentityUserRepository identityUserRepository, PasswordEncoder passwordEncoder,
                                     CredentialsGenerator credentialsGenerator, PasswordValidationService passwordValidationService, ClaimsPrincipalService claimsPrincipalService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.identityUserRepository = identityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.credentialsGenerator = credentialsGenerator;
        this.passwordValidationService = passwordValidationService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @Override
    public String getJwtTokenAsRespondent(LoginDto dto) {
        return authenticateAndGenerateToken(dto, Role.RESPONDENT);
    }

    @Override
    public String getJwtTokenAsAdmin(LoginDto dto) {
        return authenticateAndGenerateToken(dto, Role.ADMIN);
    }

    @Override
    public List<LoginDto> createRespondentsAccounts(CreateRespondentsAccountsDto dto) {
        int respondentsCount = identityUserRepository.countRespondents();
        List<LoginDto> loginDtoList = IntStream.range(1, dto.getAmount() + 1)
                .mapToObj(i -> {
                    LoginDto loginDto = new LoginDto();
                    String username = getUsernameFromNumber(i + respondentsCount);
                    loginDto.setUsername(username);
                    String randomPassword = credentialsGenerator.getRandomPassword();
                    loginDto.setPassword(randomPassword);
                    return loginDto;
                })
                .toList();
        List<IdentityUser> userList = loginDtoList
                .stream().map(loginDto -> {
                    IdentityUser respondentIdentityUser = new IdentityUser();
                    respondentIdentityUser.setRole(Role.RESPONDENT.getRoleName());
                    respondentIdentityUser.setUsername(loginDto.getUsername());
                    String passwordHash = passwordEncoder.encode(loginDto.getPassword());
                    respondentIdentityUser.setPasswordHash(passwordHash);
                    return respondentIdentityUser;
                }).toList();
        identityUserRepository.saveAll(userList);
        return loginDtoList;
    }

    @Override
    public void updateUserPassword(UUID identityUserId, ChangePasswordDto changePasswordDto) {
        IdentityUser targetUser = findIdentityUserById(identityUserId);
        updatePassword(targetUser, changePasswordDto.getNewPassword());
    }

    @Override
    public void updateOwnPassword(ChangePasswordDto changePasswordDto) {
        IdentityUser identityUser = claimsPrincipalService.findIdentityUser();
        validateOldPassword(identityUser, changePasswordDto.getOldPassword());
        updatePassword(identityUser, changePasswordDto.getNewPassword());
    }

    private String getUsernameFromNumber(int i) {
        return String.format("%05d", i);
    }

    private String authenticateAndGenerateToken(LoginDto dto, Role expectedRole) {
        IdentityUser identityUser = identityUserRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!expectedRole.getRoleName().equalsIgnoreCase(identityUser.getRole())) {
            throw new BadCredentialsException("Bad credentials");
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }
    private void validateOldPassword(IdentityUser targetUser, String oldPassword) {
        passwordValidationService.validateOldPassword(targetUser.getPasswordHash(), oldPassword);
    }
    private void updatePassword(IdentityUser identityUser, String newPassword) {
        String hashedPassword = passwordEncoder.encode(newPassword);
        identityUser.setPasswordHash(hashedPassword);
        identityUserRepository.save(identityUser);
    }
    private IdentityUser findIdentityUserById(UUID identityUserId){
        return identityUserRepository.findById(identityUserId)
                .orElseThrow(() -> new IllegalArgumentException("Respondent with given identity user id not found"));
    }
}
