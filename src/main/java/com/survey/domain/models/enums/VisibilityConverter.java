package com.survey.domain.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VisibilityConverter implements AttributeConverter<Visibility, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Visibility visibility) {
        return visibility != null ? visibility.getValue() : null;
    }

    @Override
    public Visibility convertToEntityAttribute(Integer dbValue) {
        return dbValue != null ? Visibility.fromValue(dbValue) : null;
    }
}
