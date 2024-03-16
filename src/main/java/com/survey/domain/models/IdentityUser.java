package com.survey.domain.models;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class IdentityUser {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    private String username;
    private String passwordHash;
    private String role;
}
