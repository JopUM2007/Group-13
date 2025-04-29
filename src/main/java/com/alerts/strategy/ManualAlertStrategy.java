package com.alerts.strategy;

import com.alerts.factory.ManualAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public class ManualAlertStrategy {
    ManualAlertFactory maFactory = new ManualAlertFactory();

    public void checkAlert(Patient patient, List<PatientRecord> records){
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("Alert")) {
                maFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Triggered Alert",
                        record.getTimestamp()
                );
            }
        }
    }

}
