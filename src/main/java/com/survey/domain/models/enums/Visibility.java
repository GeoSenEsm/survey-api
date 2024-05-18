package com.survey.domain.models.enums;

public enum Visibility {
    ALWAYS(0),
    GROUP_SPECIFIC(1),
    ANSWER_TRIGGERED(2);

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
