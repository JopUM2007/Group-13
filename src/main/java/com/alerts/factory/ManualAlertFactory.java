package com.alerts.factory;

import com.alerts.Alert;

public class ManualAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new ManualAlert(patientId, condition, timestamp);
    }
}
