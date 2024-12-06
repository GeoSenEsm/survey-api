package com.survey.domain.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InitialSurveyStateConverter implements AttributeConverter<InitialSurveyState, Integer> {
    @Override
    public Integer convertToDatabaseColumn(InitialSurveyState initialSurveyState) {
        if (initialSurveyState == null){
            return null;
        }
        return initialSurveyState.getValue();
    }

    @Override
    public InitialSurveyState convertToEntityAttribute(Integer dbValue) {
        if (dbValue == null){
            return null;
        }
        return InitialSurveyState.fromValue(dbValue);
    }
}
