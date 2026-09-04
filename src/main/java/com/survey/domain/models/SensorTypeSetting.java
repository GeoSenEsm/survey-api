package com.survey.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "sensor_type_setting")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorTypeSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sensor_type_id", nullable = false)
    private SensorType sensorType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "connection_timeout_seconds", nullable = false)
    private int connectionTimeoutSeconds;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
