package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters;

import com.ventrax.alerting.notifications.domain.model.valueobjects.Recipient;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RecipientAttributeConverter implements AttributeConverter<Recipient, String> {

    @Override
    public String convertToDatabaseColumn(Recipient attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Recipient convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Recipient(dbData);
    }
}
