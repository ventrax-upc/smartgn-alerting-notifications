package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters;

import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EventIdAttributeConverter implements AttributeConverter<EventId, String> {

    @Override
    public String convertToDatabaseColumn(EventId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public EventId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new EventId(dbData);
    }
}
