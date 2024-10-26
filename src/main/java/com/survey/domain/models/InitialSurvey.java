package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "initial_survey")
public class InitialSurvey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "initialSurvey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InitialSurveyQuestion> questions = new ArrayList<>();

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
