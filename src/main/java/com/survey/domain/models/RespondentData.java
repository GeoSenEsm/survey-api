package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "respondent_data")
public class RespondentData {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    private UUID identityUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private InitialSurvey surveyId;

    @OneToMany(mappedBy = "respondentData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespondentDataQuestion> respondentDataQuestions = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identityUserId", insertable = false, updatable = false)
    private IdentityUser identityUser;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "respondent_to_group",
        joinColumns = @JoinColumn(name = "respondent_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<RespondentGroup> respondentGroups = new HashSet<>();
//
//    //TODO: I really don't like this, as I feel a little bit like this is writing logic in an entity
//    //Anyways, this is a workaround, because the model mapper for some reason does not deal with getting the username by getting identity user first
//    //It throws IllegalArgumentException with message "object is not an instance of declaring class"
    public String getUsername(){
        return identityUser == null ? null : identityUser.getUsername();
    }
}

