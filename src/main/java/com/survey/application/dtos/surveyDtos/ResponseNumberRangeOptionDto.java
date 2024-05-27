package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseNumberRangeOptionDto {
    private UUID id;
    private Integer from;
    private Integer to;
    private String startLabel;
    private String endLabel;
    private Long rowVersion;
}
