package com.alerts.factory;

public class BloodOxygenAlertFactory extends AlertFactory {
    @Override
    public BloodOxygenAlert createAlert(String patientId, String condition, long timestamp) {
        return new BloodOxygenAlert(patientId, condition, timestamp);
    }
}
