package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Entity
@Data
@NoArgsConstructor
@Table(name = "respondent_data_option")
public class RespondentDataOption {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_data_question_id")
    private RespondentDataQuestion respondentDataQuestions;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "option_id")
    private InitialSurveyOption option;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
