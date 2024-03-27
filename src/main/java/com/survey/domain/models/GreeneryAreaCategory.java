package com.survey.domain.models;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class GreeneryAreaCategory {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    private String display;
    private Timestamp row_version;
}
