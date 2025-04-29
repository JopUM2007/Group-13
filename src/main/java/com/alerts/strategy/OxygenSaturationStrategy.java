package com.alerts.strategy;

import com.alerts.factory.BloodOxygenAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.Comparator;
import java.util.List;


public class OxygenSaturationStrategy implements AlertStrategy {
    private static final double SATURATION_THRESHOLD = 92.0;
    private static final double SATURATION_DROP = 5.0;
    private static final long RAPID_DROP_WINDOW_MS = 10 * 60 * 1000;

    private BloodOxygenAlertFactory boFactory = new BloodOxygenAlertFactory();

    @Override
    public void checkAlert(Patient patient, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("Saturation") && record.getMeasurementValue() < SATURATION_THRESHOLD) {
                        boFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Low Blood Saturation Alert",
                        record.getTimestamp()
                );
            }
        }


        List<PatientRecord> satRecords = records.stream()
                .filter(r -> r.getRecordType().equals("Saturation"))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .toList();

        for (int i = 0; i < satRecords.size() - 1; i++) {
            PatientRecord r1 = satRecords.get(i);
            PatientRecord r2 = satRecords.get(i + 1);
            if (r2.getTimestamp() - r1.getTimestamp() <= RAPID_DROP_WINDOW_MS) {
                double drop = r1.getMeasurementValue() - r2.getMeasurementValue();
                if (drop >= SATURATION_DROP) {
                    boFactory.createAlert(
                            String.valueOf(patient.getPatientId()),
                            "Rapid Blood Saturation Drop Alert",
                            r2.getTimestamp()
                    );
                    break;
                }
            }
        }
    }
}
