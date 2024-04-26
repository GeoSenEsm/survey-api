package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RespondentDataDto {
    @NotNull
    private UUID id;
    @NotNull
    private UUID identityUserId;
    @Pattern(regexp = "^male|female$")
    private int gender;
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
