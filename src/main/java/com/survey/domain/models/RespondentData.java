package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class RespondentData {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    private UUID identityUserId;
    private Integer gender;
    private Integer ageCategoryId;
    private Integer occupationCategoryId;
    private Integer educationCategoryId;
    private Integer greeneryAreaCategoryId;
    private Integer medicationUseId;
    private Integer healthConditionId;
    private Integer stressLevelId;
    private Integer lifeSatisfactionId;
    private Integer qualityOfSleepId;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "respondent_to_group",
        joinColumns = @JoinColumn(name = "respondent_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<RespondentGroup> respondentGroups = new HashSet<>();

}

