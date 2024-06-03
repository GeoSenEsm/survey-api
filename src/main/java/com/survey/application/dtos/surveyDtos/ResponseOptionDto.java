package com.survey.application.dtos.surveyDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseOptionDto {
    private UUID id;
    private Integer order;
    private String label;
    private Integer showSection;
    private Long rowVersion;
}
