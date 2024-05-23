package com.survey.domain.models.enums;

import java.util.Arrays;

public enum Visibility {
    always(0),
    group_specific(1),
    answer_triggered(2);

    private final int value;

    Visibility(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Visibility fromValue(int value) {
        return Arrays.stream(Visibility.values())
                .filter(visibility -> visibility.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum value: " + value));
    }
}
