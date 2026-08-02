package com.survey.domain.models.enums;

/**
 * Stable {@code sensor_type.code} values seeded in V24/V25.
 */
public final class SensorTypeCodes {
    public static final String XIAOMI = "xiaomi";
    public static final String KESTREL = "kestrel";
    /** Respondent enters temperature/humidity readings by hand (UI forthcoming). */
    public static final String MANUAL = "manual";
    /** No sensor device and no manual readings. */
    public static final String NONE = "none";

    private SensorTypeCodes() {}
}
