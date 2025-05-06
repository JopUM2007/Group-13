package com.alerts.strategy;

import com.alerts.factory.BloodPressureAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BloodPressureStrategy implements AlertStrategy{
    private static final double SYSTOLIC_UPPER = 180.0;
    private static final double SYSTOLIC_LOWER = 90.0;
    private static final double DIASTOLIC_UPPER = 120.0;
    private static final double DIASTOLIC_LOWER = 60.0;
    private static final double BP_TREND_THRESHOLD = 10.0;
    private static final long TIME_WINDOW = 86400000; // 24h in milliseconds

    private BloodPressureAlertFactory bpFactory = new BloodPressureAlertFactory();

    public void checkAlert(Patient patient, List<PatientRecord> records) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> filteredRecords = records.stream()
                .filter(r -> r.getTimestamp() >= currentTime - TIME_WINDOW)
                .collect(Collectors.toList());
        for (PatientRecord record : records) {
            double value = record.getMeasurementValue();
            if (record.getRecordType().equals("SystolicPressure")) {
                if (value > SYSTOLIC_UPPER || value < SYSTOLIC_LOWER) {
                    bpFactory.createAlert(
                            String.valueOf(patient.getPatientId()),
                            "Critical Systolic",
                            record.getTimestamp()
                    );
                }
            } else if (record.getRecordType().equals("DiastolicPressure")) {
                if (value > DIASTOLIC_UPPER || value < DIASTOLIC_LOWER) {
                    bpFactory.createAlert(
                            String.valueOf(patient.getPatientId()),
                            "Critical Diastolic",
                            record.getTimestamp()
                    );
                }
            }
        }

        checkTrend(patient, filteredRecords, "SystolicPressure");
        checkTrend(patient, filteredRecords, "DiastolicPressure");
    }

    // Modify checkTrend method
    private void checkTrend(Patient patient, List<PatientRecord> records, String type) {
        List<PatientRecord> bpRecords = records.stream()
                .filter(r -> r.getRecordType().equals(type))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .toList();

        if (bpRecords.size() < 3) return;

        // Check only the 3 most recent consecutive readings
        PatientRecord r1 = bpRecords.get(bpRecords.size()-3);
        PatientRecord r2 = bpRecords.get(bpRecords.size()-2);
        PatientRecord r3 = bpRecords.get(bpRecords.size()-1);

        if ((r2.getTimestamp() - r1.getTimestamp() < 3600000) || // 1-hour gap
                (r3.getTimestamp() - r2.getTimestamp() < 3600000)) {
            return; // Skip non-consecutive readings
        }

        boolean increasing = (r2.getMeasurementValue() - r1.getMeasurementValue() >= BP_TREND_THRESHOLD)
                && (r3.getMeasurementValue() - r2.getMeasurementValue() >= BP_TREND_THRESHOLD);

        boolean decreasing = (r1.getMeasurementValue() - r2.getMeasurementValue() >= BP_TREND_THRESHOLD)
                && (r2.getMeasurementValue() - r3.getMeasurementValue() >= BP_TREND_THRESHOLD);

        if (increasing) {
            bpFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    type.replace("Pressure","") + " Pressure Increasing", // "Systolic Pressure Increasing"
                    r3.getTimestamp()
            );
        }
        if (decreasing) {
            bpFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    type.replace("Pressure","") + " Pressure Decreasing",
                    r3.getTimestamp()
            );
        }
    }

}
