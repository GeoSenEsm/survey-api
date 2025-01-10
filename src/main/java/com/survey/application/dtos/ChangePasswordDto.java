package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {
    @Schema(description = "Users current password. Must be valid when respondent/admin are changing their own password. Not required when admin is changing respondent's password.",
            example = "my-current-password123")
    private String oldPassword;

    @NotBlank
    @Size(min = 12, max = 60)
    @Schema(description = "Users new password.",
            example = "my-new-password456")
    private String newPassword;
}
