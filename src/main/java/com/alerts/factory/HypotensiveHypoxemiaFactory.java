package com.alerts.factory;

import com.alerts.Alert;

public class HypotensiveHypoxemiaFactory extends AlertFactory {
    /**
     * Creates an Hypotensive Hypoxemia alert with the given details.
     *
     * @param patientId ID of the patient
     * @param condition Condition that triggered the alert
     * @param timestamp Time of when the alert was triggered
     * @return new Hypotensive Hypoxemia Alert object
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new HypotensiveHypoxemiaAlert(patientId, condition, timestamp);
    }
}
