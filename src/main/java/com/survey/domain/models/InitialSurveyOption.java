package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "initial_survey_option")
public class InitialSurveyOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private InitialSurveyQuestion question;

    @Column(name = "[order]")
    private int order;

    @Column(name = "content", nullable = false)
    private String content;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
