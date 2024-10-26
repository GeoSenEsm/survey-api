package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "initial_survey_question")
public class InitialSurveyQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "[order]")
    private int order;

    @Column(name = "content", nullable = false)
    private String content;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InitialSurveyOption> options = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "survey_id", nullable = false)
    private InitialSurvey initialSurvey;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
