package com.alerts.decorators;

import com.alerts.Alert;
import com.alerts.AlertGenerator;

public abstract class AlertDecorator {
    private AlertGenerator alertGenerator;

    protected Alert alert;

    public void triggerAlert() {
    }
}
