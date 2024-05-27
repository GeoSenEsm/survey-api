package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.ValidQuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateQuestionDto {

    @NotNull
    @Min(1)
    @Max(9999)
    private Integer order;

    @NotBlank
    @Size(min = 1, max = 250)
    private String content;

    @NotNull
    @ValidQuestionType
    private String questionType;

    @NotNull
    private boolean required;

    @Size(min = 2)
    private List<@Valid CreateOptionDto> options;

    private @Valid CreateNumberRangeOptionDto numberRange;

}
