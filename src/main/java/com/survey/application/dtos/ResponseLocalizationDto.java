package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ResponseLocalizationDto {
    private UUID id;
    private UUID surveyParticipationId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long rowVersion;
}
