package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class CreateRespondentDataDto {
    @Pattern(regexp = "^male|female$")
    private String gender;
    @NotNull
    private Integer ageCategoryId;
    @NotNull
    private Integer occupationCategoryId;
    @NotNull
    private Integer educationCategoryId;
    @NotNull
    private Integer healthConditionId;
    @NotNull
    private Integer medicationUseId;
    @NotNull
    private Integer lifeSatisfactionId;
    @NotNull
    private Integer stressLevelId;
    @NotNull
    private Integer qualityOfSleepId;
    @NotNull
    private Integer greeneryAreaCategoryId;
}
