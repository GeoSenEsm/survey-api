package com.survey.domain.models.enums;

public enum QuestionType {
    single_text_selection(0),
    discrete_number_selection(1);

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
