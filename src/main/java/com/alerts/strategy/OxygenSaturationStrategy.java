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
        boolean lowSatAlertCreated = false;
        PatientRecord earliestLowRecord = null;
        PatientRecord prevRecord = null;

        // Process in a single pass, tracking both conditions
        List<PatientRecord> satRecords = records.stream()
                .filter(r -> r.getRecordType().equals("Saturation"))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .toList();

        for (PatientRecord record : satRecords) {
            // Check for low saturation
            if (!lowSatAlertCreated && record.getMeasurementValue() < SATURATION_THRESHOLD) {
                boFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Low Blood Saturation Alert",
                    record.getTimestamp()
                );
                lowSatAlertCreated = true;
            }

            // Check for rapid drop
            if (prevRecord != null &&
                record.getTimestamp() - prevRecord.getTimestamp() <= RAPID_DROP_WINDOW_MS) {

                double drop = prevRecord.getMeasurementValue() - record.getMeasurementValue();
                if (drop >= SATURATION_DROP) {
                    boFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Rapid Blood Saturation Drop Alert",
                        record.getTimestamp()
                    );
                    break; // Stop after first rapid drop
                }
            }

            prevRecord = record;
        }
    }
}
