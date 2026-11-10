package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters;

import com.ventrax.alerting.notifications.domain.model.valueobjects.PropertyId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter
public class PropertyIdAttributeConverter implements AttributeConverter<PropertyId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(PropertyId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PropertyId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new PropertyId(dbData);
    }
}
