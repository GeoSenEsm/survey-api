package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SurveyResultDto {
    private String surveyName;
    private String question;
    private LocalDate responseDate;
    private List<String> answers;
    private UUID respondentId;
}
