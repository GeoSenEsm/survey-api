package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class MedicationUse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String display;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    public MedicationUse(String display){this.display = display;}
}
