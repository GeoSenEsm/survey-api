package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class RespondentDataDto {
    private UUID id;
    private UUID identityUserId;
    private String username;
    private String gender;
    private Integer ageCategoryId;
    private Integer occupationCategoryId;
    private Integer educationCategoryId;
    private Integer greeneryAreaCategoryId;
    private Integer medicationUseId;
    private Integer healthConditionId;
    private Integer stressLevelId;
    private Integer lifeSatisfactionId;
    private Integer qualityOfSleepId;
}
