package com.alerts.strategy;

import com.alerts.factory.ManualAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public class ManualAlertStrategy implements AlertStrategy{
    ManualAlertFactory maFactory = new ManualAlertFactory();

    public void checkAlert(Patient patient, List<PatientRecord> records) {

            long currentTime = System.currentTimeMillis();
            long timeWindow = 24 * 60 * 60 * 1000; // 24 hours
            boolean alertCreated = false; // Flag to create only one alert within time window

            for (PatientRecord record : records) {
                if (currentTime - record.getTimestamp() <= timeWindow &&
                    "Alert".equals(record.getRecordType()) &&
                    !alertCreated) {

                    maFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Triggered Alert",
                        record.getTimestamp()
                    );
                    alertCreated = true; // Only create one alert per check
                }
            }
    }
}
