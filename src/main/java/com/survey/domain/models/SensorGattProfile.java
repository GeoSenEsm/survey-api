package com.survey.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sensor_gatt_profile")
@Getter
@Setter
@NoArgsConstructor
public class SensorGattProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_type_id", nullable = false)
    private SensorType sensorType;

    @Column(nullable = false)
    private int revision;

    @Column(length = 16, nullable = false)
    private String status;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;

    @Column(name = "spec_json", columnDefinition = "nvarchar(max)", nullable = false)
    private String specJson;

    @Column(name = "spec_hash", length = 64, nullable = false)
    private String specHash;

    @Column(name = "min_engine_version", length = 32, nullable = false)
    private String minEngineVersion;

    @Column(name = "read_only", nullable = false)
    private boolean readOnly;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Version
    @Column(name = "row_version", insertable = false, updatable = false)
    private byte[] rowVersion;
}
