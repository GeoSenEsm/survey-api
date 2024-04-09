package com.survey.domain.models;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class GreeneryAreaCategory {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;
    private String display;
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    public GreeneryAreaCategory(String display) {
        this.display = display;
    }
}
