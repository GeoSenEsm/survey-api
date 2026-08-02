package com.survey.domain.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotificationRelativeToConverter implements AttributeConverter<NotificationRelativeTo, Integer> {
    @Override
    public Integer convertToDatabaseColumn(NotificationRelativeTo attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public NotificationRelativeTo convertToEntityAttribute(Integer dbData) {
        return dbData != null ? NotificationRelativeTo.fromValue(dbData) : null;
    }
}
