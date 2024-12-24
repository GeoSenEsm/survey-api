package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class TextAnswer {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_answer_id", nullable = false, unique = true)
    private QuestionAnswer questionAnswer;

    @Column(name = "text_answer_content", nullable = false, length = 150)
    private String textAnswerContent;

    @Version
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
