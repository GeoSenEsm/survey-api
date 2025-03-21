package com.survey.application.dtos;

import com.survey.api.validation.ValidSurveyParticipationId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Used to send geo localization reading by respondent.")
public class LocalizationDataDto {

    @ValidSurveyParticipationId
    @Schema(description = "(optional) When localization data was measured while filling survey it is possible to bind given localization point with survey participation.")
    private UUID surveyParticipationId;

    @NotNull
    @Schema(description = "Date and time in UTC when given localization data was measured.")
    private OffsetDateTime dateTime;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Digits(integer = 2, fraction = 6)
    @Schema(description = "Precision up to 6 decimal places.",
            example = "52.228851")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Digits(integer = 3, fraction = 6)
    @Schema(description = "Precision up to 6 decimal places.",
            example = "21.020921")
    private BigDecimal longitude;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "999999.99")
    @Digits(integer = 6, fraction = 2)
    @Schema(description = "Accuracy of location measurement in meters. Max value 999999.99",
            example = "20.50")
    private BigDecimal accuracyMeters;

}
