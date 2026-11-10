package com.ventrax.alerting.notifications.infrastructure.persistence.jpa.repositories;

import com.ventrax.alerting.notifications.domain.model.aggregates.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findAllByOrderByNameAsc();

    List<AlertRule> findAllByActiveTrueOrderByNameAsc();

    List<AlertRule> findAllByActiveFalseOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
