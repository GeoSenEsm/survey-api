package com.survey.domain.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Gender gender) {
        if (gender == null){
            return null;
        }
        return gender.getValue();
    }

    @Override
    public Gender convertToEntityAttribute(Integer dbValue) {
        if (dbValue == null){
            return null;
        }
        return Gender.fromValue(dbValue);
    }
}
