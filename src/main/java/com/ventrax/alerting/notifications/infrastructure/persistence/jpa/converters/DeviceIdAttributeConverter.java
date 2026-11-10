package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters;

import com.ventrax.alerting.notifications.domain.model.valueobjects.DeviceId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DeviceIdAttributeConverter implements AttributeConverter<DeviceId, String> {

    @Override
    public String convertToDatabaseColumn(DeviceId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public DeviceId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new DeviceId(dbData);
    }
}
