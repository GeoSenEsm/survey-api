package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class ResponseResearchAreaDto {
    private UUID id;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long rowVersion;
}
