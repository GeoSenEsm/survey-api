package com.survey.domain.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.criteria.CriteriaBuilder;

@Converter(autoApply = true)
public class QuestionTypeConverter implements AttributeConverter<QuestionType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(QuestionType questionType) {
        if (questionType == null){
            return null;
        }
        return questionType.getValue();
    }

    @Override
    public QuestionType convertToEntityAttribute(Integer dbValue) {
        if (dbValue == null){
            return null;
        }
        return QuestionType.fromValue(dbValue);
    }
}
