package com.survey.domain.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SurveyStateConverter implements AttributeConverter<SurveyState, Integer> {
    @Override
    public Integer convertToDatabaseColumn(SurveyState surveyState) {
        if (surveyState == null){
            return null;
        }
        return surveyState.getValue();
    }

    @Override
    public SurveyState convertToEntityAttribute(Integer dbValue) {
        if (dbValue == null){
            return null;
        }
        return SurveyState.fromValue(dbValue);
    }
}
