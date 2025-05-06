package com.alerts.factory;

public class BloodOxygenAlertFactory extends AlertFactory {
    /**
     * Creates a Blood Oxygen alert with the given details.
     *
     * @param patientId ID of the patient
     * @param condition Condition triggering the alert
     * @param timestamp Time of the alert
     * @return Created Blood Oxygen Alert object
     */
    @Override
    public BloodOxygenAlert createAlert(String patientId, String condition, long timestamp) {
        return new BloodOxygenAlert(patientId, condition, timestamp);
    }
}
