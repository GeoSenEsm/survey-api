package com.survey.domain.models.enums;

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
        for (Visibility visibility : values()) {
            if (visibility.value == value) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + value);
    }
}
