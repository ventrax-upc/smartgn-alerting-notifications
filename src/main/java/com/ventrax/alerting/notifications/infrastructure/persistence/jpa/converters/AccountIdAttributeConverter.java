package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.converters;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AccountId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter
public class AccountIdAttributeConverter implements AttributeConverter<AccountId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(AccountId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public AccountId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new AccountId(dbData);
    }
}
