package com.alerts.strategy;

import com.alerts.factory.ECGAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.Comparator;
import java.util.List;

public class HeartRateStrategy implements AlertStrategy {
    private static final double ECG_PEAK_FACTOR = 2.0;
    private static final int ECG_WINDOW = 10;

    ECGAlertFactory ecgFactory = new ECGAlertFactory();

    public void checkAlert(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecgRecords = records.stream()
                .filter(r -> r.getRecordType().equals("ECG"))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .toList();

        if (ecgRecords.size() <= ECG_WINDOW) return;

        for (int i = ECG_WINDOW; i < ecgRecords.size(); i++) {
            double sum = 0;
            for (int j = i - ECG_WINDOW; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double avg = sum / ECG_WINDOW;
            double val = ecgRecords.get(i).getMeasurementValue();
            if (val > avg * ECG_PEAK_FACTOR) {
                        ecgFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "ECG Abnormal Peak Alert",
                        ecgRecords.get(i).getTimestamp()
                );
                break;
            }
        }
    }
}
