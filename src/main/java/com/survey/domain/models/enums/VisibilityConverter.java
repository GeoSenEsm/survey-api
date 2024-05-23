package com.survey.domain.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VisibilityConverter implements AttributeConverter<Visibility, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Visibility visibility) {
        if (visibility == null){
            return null;
        }
        return visibility.getValue();
    }

    @Override
    public Visibility convertToEntityAttribute(Integer dbValue) {
        if (dbValue == null){
            return null;
        }
        return Visibility.fromValue(dbValue);
    }
}
