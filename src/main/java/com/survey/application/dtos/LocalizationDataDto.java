package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class LocalizationDataDto {
    @NotNull
    private UUID surveyParticipationId;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;
}
