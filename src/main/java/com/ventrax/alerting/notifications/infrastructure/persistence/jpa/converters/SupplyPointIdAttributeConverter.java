package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters;

import com.ventrax.alerting.notifications.domain.model.valueobjects.SupplyPointId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter
public class SupplyPointIdAttributeConverter implements AttributeConverter<SupplyPointId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(SupplyPointId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public SupplyPointId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new SupplyPointId(dbData);
    }
}
