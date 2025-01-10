package com.survey.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AllResultsLocalizationDataDto {
    private UUID localizationDataId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private OffsetDateTime dateTime;
    private Boolean outsideResearchArea;
    private UUID surveyParticipationId;
}
