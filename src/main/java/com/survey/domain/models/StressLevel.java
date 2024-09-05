package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class StressLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String polishDisplay;
    private String englishDisplay;
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
