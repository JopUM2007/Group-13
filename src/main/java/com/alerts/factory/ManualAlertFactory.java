package com.alerts.factory;

import com.alerts.Alert;

public class ManualAlertFactory extends AlertFactory{
    /**
     * Creates a Manual alert with the given details.
     *
     * @param patientId Id of the patient
     * @param condition Condition that triggered the alert
     * @param timestamp Time of when the alert was triggered
     * @return new manual alert object
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new ManualAlert(patientId, condition, timestamp);
    }
}
