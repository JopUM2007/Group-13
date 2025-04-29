package com.alerts.factory;

import com.alerts.Alert;

public class ECGAlert extends Alert {
    public ECGAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
        System.out.printf(
                "[ALERT] Patient %s | Condition: %s | Timestamp: %d%n",
                patientId,
                condition,
                timestamp
        );
    }
}
