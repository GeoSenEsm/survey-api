package com.survey.domain.models.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum NotificationRelativeTo {
    beginning(0),
    end(1);

    private final int value;

    NotificationRelativeTo(int value) {
        this.value = value;
    }

    public static NotificationRelativeTo fromValue(int value) {
        return Arrays.stream(values())
                .filter(relativeTo -> relativeTo.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown NotificationRelativeTo value: " + value));
    }
}
