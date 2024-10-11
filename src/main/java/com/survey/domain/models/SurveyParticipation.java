package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class SurveyParticipation {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id")
    private IdentityUser identityUser;

    @Column(name = "date")
    private OffsetDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    @OneToMany(mappedBy = "surveyParticipation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswer> questionAnswers = new ArrayList<>();

}
