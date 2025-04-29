package com.alerts.factory;

import com.alerts.Alert;

public class BloodOxygenAlert extends Alert {
    public BloodOxygenAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
        System.out.printf(
                "[ALERT] Patient %s | Condition: %s | Timestamp: %d%n",
                patientId,
                condition,
                timestamp
        );
    }
}
