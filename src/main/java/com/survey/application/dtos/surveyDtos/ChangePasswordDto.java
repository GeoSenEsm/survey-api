package com.survey.application.dtos.surveyDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {
    private String oldPassword;

    @NotBlank
    @Size(min = 12, max = 60)
    private String newPassword;
}
