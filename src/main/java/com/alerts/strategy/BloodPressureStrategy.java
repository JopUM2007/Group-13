package com.alerts.strategy;

import com.alerts.factory.BloodPressureAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.Comparator;
import java.util.List;

public class BloodPressureStrategy implements AlertStrategy{
    private static final double SYSTOLIC_UPPER = 180.0;
    private static final double SYSTOLIC_LOWER = 90.0;
    private static final double DIASTOLIC_UPPER = 120.0;
    private static final double DIASTOLIC_LOWER = 60.0;
    private static final double BP_TREND_THRESHOLD = 10.0;

    private BloodPressureAlertFactory bpFactory = new BloodPressureAlertFactory();

    public void checkAlert(Patient patient, List<PatientRecord> records) {
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

        checkTrend(patient, records, "SystolicPressure");
        checkTrend(patient, records, "DiastolicPressure");
    }

    private void checkTrend(Patient patient, List<PatientRecord> records, String type) {
        List<PatientRecord> bpRecords = records.stream()
                .filter(r -> r.getRecordType().equals(type))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .toList();

        for (int i = 0; i < bpRecords.size() - 2; i++) {
            double v1 = bpRecords.get(i).getMeasurementValue();
            double v2 = bpRecords.get(i + 1).getMeasurementValue();
            double v3 = bpRecords.get(i + 2).getMeasurementValue();
            // Increasing Trend
            if (v2 - v1 > BP_TREND_THRESHOLD && v3 - v2 > BP_TREND_THRESHOLD){
                bpFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Blood Pressure Trend Alert",
                        bpRecords.get(i+2).getTimestamp()
                );
            }
            // Decreasing Trend
            else if (v1 - v2 > BP_TREND_THRESHOLD && v2 - v3 > BP_TREND_THRESHOLD) {
                bpFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Blood Pressure Trend Alert",
                        bpRecords.get(i+2).getTimestamp()
                );
            }
        }
    }
}
