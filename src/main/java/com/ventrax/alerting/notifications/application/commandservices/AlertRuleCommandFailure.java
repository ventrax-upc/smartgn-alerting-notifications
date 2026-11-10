package com.ventrax.alerting.notifications.application.commandservices;

public sealed interface AlertRuleCommandFailure
        permits AlertRuleCommandFailure.Duplicate,
        AlertRuleCommandFailure.NotFound,
        AlertRuleCommandFailure.Invalid {

    String messageKey();

    record Duplicate() implements AlertRuleCommandFailure {
        @Override
        public String messageKey() {
            return "alert.rule.error.duplicate";
        }
    }

    record NotFound(Long ruleId) implements AlertRuleCommandFailure {
        @Override
        public String messageKey() {
            return "alert.rule.error.notFound";
        }
    }

    record Invalid(String messageKey) implements AlertRuleCommandFailure {
    }
}
