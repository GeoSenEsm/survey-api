package com.survey.application.dtos.surveyDtos;

import com.survey.domain.models.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseQuestionDto {
    private UUID id;
    private Integer order;
    private String content;
    private QuestionType questionType;
    private boolean required;
    private Long rowVersion;

    private List<ResponseOptionDto> options;
}
