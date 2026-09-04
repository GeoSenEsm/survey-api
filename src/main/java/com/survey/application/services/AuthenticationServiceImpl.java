package com.survey.application.services;

import com.survey.api.security.Role;
import com.survey.api.security.TokenProvider;
import com.survey.application.dtos.CreateRespondentsAccountsDto;
import com.survey.application.dtos.LoginDto;
import com.survey.application.dtos.ChangePasswordDto;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final IdentityUserRepository identityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialsGenerator credentialsGenerator;
    private final PasswordValidationService passwordValidationService;
    private final ClaimsPrincipalService claimsPrincipalService;
    private final RespondentTimeZoneService respondentTimeZoneService;
    private final LocalParticipationRecalculationService localParticipationRecalculationService;


    @Autowired
    public AuthenticationServiceImpl(AuthenticationManager authenticationManager, TokenProvider tokenProvider,
                                     IdentityUserRepository identityUserRepository, PasswordEncoder passwordEncoder,
                                     CredentialsGenerator credentialsGenerator, PasswordValidationService passwordValidationService,
                                     ClaimsPrincipalService claimsPrincipalService,
                                     RespondentTimeZoneService respondentTimeZoneService,
                                     LocalParticipationRecalculationService localParticipationRecalculationService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.identityUserRepository = identityUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.credentialsGenerator = credentialsGenerator;
        this.passwordValidationService = passwordValidationService;
        this.claimsPrincipalService = claimsPrincipalService;
        this.respondentTimeZoneService = respondentTimeZoneService;
        this.localParticipationRecalculationService = localParticipationRecalculationService;
    }

    /**
     * The timezone update and its participation recalculation are best-effort: the credentials
     * were already validated and a token already minted by the time this runs, so a failure here
     * (a bad timezone id slipping past validation, a transient DB/Mongo hiccup) must not turn an
     * otherwise-successful login into an error for the respondent.
     */
    @Override
    public String getJwtTokenAsRespondent(LoginDto dto) {
        String token = authenticateAndGenerateToken(dto, Role.RESPONDENT);
        try {
            applyRespondentTimeZoneAfterLogin(dto);
        } catch (Exception ex) {
            log.warn("Failed to apply respondent timezone after login for user '{}'", dto.getUsername(), ex);
        }
        return token;
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
                    respondentIdentityUser.setTimeZone(RespondentTimeZoneService.DEFAULT_TIME_ZONE);
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

    private void applyRespondentTimeZoneAfterLogin(LoginDto dto) {
        IdentityUser user = identityUserRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        String previous = user.getTimeZone() == null || user.getTimeZone().isBlank()
                ? RespondentTimeZoneService.DEFAULT_TIME_ZONE
                : user.getTimeZone();
        String next;
        try {
            next = dto.getTimeZone() == null || dto.getTimeZone().isBlank()
                    ? previous
                    : respondentTimeZoneService.normalizeOrDefault(dto.getTimeZone());
        } catch (IllegalArgumentException ex) {
            next = previous;
        }

        if (Objects.equals(previous, next) && Objects.equals(user.getTimeZone(), next)) {
            return;
        }
        user.setTimeZone(next);
        identityUserRepository.save(user);
        localParticipationRecalculationService.recalculateForRespondent(user.getId());
    }

    private String getUsernameFromNumber(int i) {
        return String.format("%05d", i);
    }

    private String authenticateAndGenerateToken(LoginDto dto, Role expectedRole) {
        IdentityUser identityUser = identityUserRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!identityUser.getUsername().equals(dto.getUsername())){
            throw new BadCredentialsException("Bad credentials");
        }

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
