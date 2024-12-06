package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class OptionSelection {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_answer_id")
    private QuestionAnswer questionAnswer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "option_id")
    private Option option;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
