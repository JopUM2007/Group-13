package com.alerts.strategy;

import com.alerts.factory.HypotensiveHypoxemiaFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public class HypotensiveHypoxemiaStrategy implements AlertStrategy{
    private static final double SYSTOLIC_LOWER = 90.0;
    private static final double SATURATION_THRESHOLD = 92.0;

    private HypotensiveHypoxemiaFactory hhFactory = new HypotensiveHypoxemiaFactory();

    public void checkAlert(Patient patient, List<PatientRecord> records) {
        double lastSystolic = Double.NaN;
        double lastSaturation = Double.NaN;
        long lastSystolicTime = 0, lastSaturationTime = 0;
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("SystolicPressure")) {
                lastSystolic = record.getMeasurementValue();
                lastSystolicTime = record.getTimestamp();
            } else if (record.getRecordType().equals("Saturation")) {
                lastSaturation = record.getMeasurementValue();
                lastSaturationTime = record.getTimestamp();
            }
        }
        if (!Double.isNaN(lastSystolic) && !Double.isNaN(lastSaturation)
                && lastSystolic < SYSTOLIC_LOWER && lastSaturation < SATURATION_THRESHOLD) {
            hhFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia Alert",
                    Math.max(lastSystolicTime, lastSaturationTime)
            );
        }

    }
}
