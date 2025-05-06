package com.alerts.factory;

import com.alerts.Alert;

public abstract class AlertFactory {

    /**
     * Creates an alert with the given details.
     *
     * @param patientId Id of the patient
     * @param condition Condition that triggered the alert
     * @param timestamp Time of when alert was triggered
     * @return new Alert object
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
