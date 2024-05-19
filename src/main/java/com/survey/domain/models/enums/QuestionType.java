package com.survey.domain.models.enums;

public enum QuestionType {
    SINGLE_TEXT_SELECTION(0),
    DISCRETE_NUMBER_SELECTION(1);

    private final int value;

    QuestionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static QuestionType fromValue(int value) {
        for (QuestionType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + value);
    }
}
