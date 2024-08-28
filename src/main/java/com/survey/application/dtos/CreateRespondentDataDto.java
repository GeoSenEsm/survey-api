package com.survey.application.dtos;

import com.survey.api.validation.ValidGender;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
public class CreateRespondentDataDto {
    @NotNull
    @ValidGender
    private String gender;
    @NotNull
    private Integer ageCategoryId;
    @NotNull
    private Integer occupationCategoryId;
    @NotNull
    private Integer educationCategoryId;
    @NotNull
    private Integer greeneryAreaCategoryId;
    @NotNull
    private Integer medicationUseId;
    @NotNull
    private Integer healthConditionId;
    @NotNull
    private Integer stressLevelId;
    @NotNull
    private Integer lifeSatisfactionId;
    @NotNull
    private Integer qualityOfSleepId;
}
