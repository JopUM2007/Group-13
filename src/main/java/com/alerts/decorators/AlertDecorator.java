package com.alerts.decorators;

import com.alerts.Alert;

public class AlertDecorator extends Alert {
    protected Alert wrappedAlert;

    public AlertDecorator(Alert alert) {
        super(alert.getPatientId(), alert.getCondition(), alert.getTimestamp());
        this.wrappedAlert = alert;
    }

    @Override
    public String getPatientId() {
        return wrappedAlert.getPatientId();
    }

    @Override
    public String getCondition() {
        return wrappedAlert.getCondition();
    }

    @Override
    public long getTimestamp() {
        return wrappedAlert.getTimestamp();
    }
}
