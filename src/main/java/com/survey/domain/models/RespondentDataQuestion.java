package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "respondent_data_question")
public class RespondentDataQuestion {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id")
    private RespondentData respondentData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private InitialSurveyQuestion question;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    @ManyToMany(mappedBy = "respondentDataQuestions", cascade = CascadeType.ALL)
    private List<RespondentDataOption> options = new ArrayList<>();
}
