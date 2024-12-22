package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_answer")
@Accessors(chain = true)
public class QuestionAnswer {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id")
    private SurveyParticipation surveyParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "numeric_answer")
    private Integer numericAnswer;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    @ManyToMany(mappedBy = "questionAnswer", cascade = CascadeType.ALL)
    private List<OptionSelection> optionSelections = new ArrayList<>();

    @Column(name = "yes_no_answer")
    private Boolean yesNoAnswer;

    @OneToOne(mappedBy = "questionAnswer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TextAnswer textAnswer;
}
