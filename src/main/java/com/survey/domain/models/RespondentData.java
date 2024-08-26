package com.survey.domain.models;

import com.survey.domain.models.enums.Gender;
import com.survey.domain.models.enums.GenderConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RespondentData {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    private UUID identityUserId;

    @Convert(converter = GenderConverter.class)
    private Gender gender;
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

