package com.alerts.decorators;

public class PriorityAlertDecorator extends AlertDecorator {
    @Override
    public void triggerAlert() {
        super.triggerAlert();
        System.out.println("Priority alert triggered.");
    }

}
