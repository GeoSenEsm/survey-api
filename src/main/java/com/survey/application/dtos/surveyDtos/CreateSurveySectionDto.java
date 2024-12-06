package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.ValidVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSurveySectionDto {

    @NotNull
    @Min(1)
    @Max(9999)
    private Integer order;

    @Size(max = 100) // it can be null
    private String name;

    @NotNull
    @ValidVisibility
    private String visibility;

    private String groupId; // it also can be null

    @NotNull
    private boolean displayOnOneScreen;

    @NotEmpty
    private List<@Valid CreateQuestionDto> questions;

}
