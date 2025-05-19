package com.alerts.factory;

import com.alerts.Alert;

public class BloodPressureAlertFactory extends AlertFactory {
    /**
     * Creates a Blood Pressure alert with the given details.
     *
     * @param patientId ID of the patient
     * @param condition Condition that triggered the alert
     * @param timestamp Time of when the alert was triggered
     * @return new Blood Pressure Alert object
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodPressureAlert(patientId, condition, timestamp);
    }
}
