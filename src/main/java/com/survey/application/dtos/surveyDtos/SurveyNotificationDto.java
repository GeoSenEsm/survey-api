package com.survey.application.dtos.surveyDtos;

import com.survey.domain.models.enums.NotificationRelativeTo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Phone notification scheduled relative to a survey time-slot window.")
public class SurveyNotificationDto {

    @Schema(description = "UUID of the notification rule. Assigned by the server; ignored on replace.")
    private UUID id;

    @Schema(description = "Display / apply order within the survey.", example = "0")
    @Min(0)
    @Max(99)
    private int order;

    @NotNull
    @Schema(description = "Anchor point within the time slot.",
            allowableValues = {"beginning", "end"},
            example = "beginning")
    private NotificationRelativeTo relativeTo;

    @Min(0)
    @Max(24 * 60)
    @Schema(description = "Minutes before the anchor when the notification fires. 0 means exactly at the anchor.",
            example = "15")
    private int minutesBefore;

    @Schema(example = "2001")
    private Long rowVersion;
}
