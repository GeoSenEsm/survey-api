package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class RespondentData {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    //private UUID identityUserId;


    private Integer genderId;

    private Integer ageCategoryId;
    private Integer occupationCategoryId;
    private Integer educationCategoryId;
    private Integer healthConditionId;
    private Integer medicationUseId;
    private Integer lifeSatisfactionId;
    private Integer stressLevelId;
    private Integer qualityOfSleepId;
}

