package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ResponseLocalizationDto {
    private UUID id;
    private UUID respondentId;
    private UUID surveyParticipationId;
    private UUID surveyId;
    private OffsetDateTime dateTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long rowVersion;
}
