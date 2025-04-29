package com.alerts.factory;

import com.alerts.Alert;

public class HypotensiveHypoxemiaAlert extends Alert {
    public HypotensiveHypoxemiaAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
        System.out.printf(
                "[ALERT] Patient %s | Condition: %s | Timestamp: %d%n",
                patientId,
                condition,
                timestamp
        );
    }
}
