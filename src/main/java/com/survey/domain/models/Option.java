package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "[option]", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order", "question_id"}),
        @UniqueConstraint(columnNames = {"label", "question_id"})
})
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "[order]", nullable = false)
    private Integer order;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String label;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
