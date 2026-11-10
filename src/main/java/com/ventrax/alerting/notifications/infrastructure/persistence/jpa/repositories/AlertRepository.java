package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories;

import com.ventrax.alerting.notifications.domain.model.aggregates.Alert;
import com.ventrax.alerting.notifications.domain.model.valueobjects.DeviceId;
import com.ventrax.alerting.notifications.domain.model.valueobjects.EventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    boolean existsByDeviceIdAndEventIdAndRuleId(DeviceId deviceId, EventId eventId, Long ruleId);
}
