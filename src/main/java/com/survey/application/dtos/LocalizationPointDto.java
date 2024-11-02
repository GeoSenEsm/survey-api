package com.survey.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LocalizationPointDto {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private OffsetDateTime dateTime;
}
