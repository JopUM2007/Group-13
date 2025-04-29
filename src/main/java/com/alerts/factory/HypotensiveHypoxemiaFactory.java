package com.alerts.factory;

import com.alerts.Alert;

public class HypotensiveHypoxemiaFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new HypotensiveHypoxemiaAlert(patientId, condition, timestamp);
    }
}
