package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "[option]", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order", "question_id"}),
        @UniqueConstraint(columnNames = {"label", "question_id"})
})
@AllArgsConstructor
@Accessors(chain = true)
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "[order]", nullable = false)
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String label;

    private Integer showSection;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;


}
