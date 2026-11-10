package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters;

import com.ventrax.alerting.notifications.domain.model.valueobjects.Threshold;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter
public class ThresholdAttributeConverter implements AttributeConverter<Threshold, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Threshold attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Threshold convertToEntityAttribute(BigDecimal dbData) {
        return dbData == null ? null : new Threshold(dbData);
    }
}
