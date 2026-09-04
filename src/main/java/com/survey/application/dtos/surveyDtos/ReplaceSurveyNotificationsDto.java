package com.survey.application.dtos.surveyDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Replaces the full set of phone notifications for a survey.")
public class ReplaceSurveyNotificationsDto {

    @NotNull
    @Size(max = 10)
    @Valid
    @Schema(description = "Notification rules. Empty list disables notifications for the survey. Max 10.")
    private List<SurveyNotificationDto> notifications;
}
