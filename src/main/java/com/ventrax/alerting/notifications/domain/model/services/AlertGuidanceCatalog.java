package com.ventrax.alerting.notifications.domain.model.services;

import com.ventrax.alerting.notifications.domain.model.valueobjects.AlertSeverity;
import com.ventrax.alerting.notifications.domain.model.valueobjects.RuleMetric;

/**
 * Predefined safety guidance attached to an alert (US22).
 * SmartGN never operates valves remotely; the guidance is informational only.
 */
public final class AlertGuidanceCatalog {

    private AlertGuidanceCatalog() {
    }

    public static String instructionsFor(RuleMetric metric, AlertSeverity severity) {
        if (metric == null) {
            return generalInstructions();
        }
        return switch (metric) {
            case GAS_PRESENCE -> """
                    1. No encienda ni apague artefactos eléctricos.
                    2. Cierre la válvula general de gas si es seguro hacerlo.
                    3. Abra puertas y ventanas para ventilar el ambiente.
                    4. Evacúe el inmueble y avise a las personas cercanas.
                    5. Comuníquese con el distribuidor de gas y con los servicios de emergencia.
                    """;
            case PRESSURE -> """
                    1. Cierre la válvula general de gas si es seguro hacerlo.
                    2. Evite el uso de llamas y artefactos que generen chispas.
                    3. Ventile el ambiente y mantenga alejadas a las personas.
                    4. Comuníquese con el distribuidor de gas para reportar la condición.
                    """;
            case CONSUMPTION -> """
                    1. Revise si hay artefactos de gas encendidos sin necesidad.
                    2. Verifique posibles fugas en conexiones visibles.
                    3. Si el consumo no se explica, solicite una revisión técnica.
                    """;
            case VALVE_STATE -> """
                    La válvula aparece abierta. Una válvula abierta por sí sola no confirma una fuga.
                    1. Verifique si el gas se encuentra en uso.
                    2. Si no hay consumo, cierre la válvula y solicite una revisión.
                    3. Ante olor a gas, siga las instrucciones de emergencia y contacte al distribuidor.
                    """;
        };
    }

    private static String generalInstructions() {
        return """
                1. Mantenga la calma y ventile el ambiente.
                2. Evite llamas y equipos que generen chispas.
                3. Comuníquese con el distribuidor de gas y con los servicios de emergencia.
                """;
    }
}
