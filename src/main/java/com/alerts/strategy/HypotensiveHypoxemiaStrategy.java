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
        long currentTime = System.currentTimeMillis();
        long timeWindow = 30 * 60 * 1000; // 30 minutes in milliseconds

        Double recentSystolic = null;
        Double recentSaturation = null;
        long recentSystolicTime = 0;
        long recentSaturationTime = 0;

        // Process records in existing order to avoid sorting
        for (PatientRecord record : records) {
            // Skip older readings
            if (currentTime - record.getTimestamp() > timeWindow) {
                continue;
            }

            String recordType = record.getRecordType();
            if ("SystolicPressure".equals(recordType) &&
                (recentSystolic == null || record.getTimestamp() > recentSystolicTime)) {
                recentSystolic = record.getMeasurementValue();
                recentSystolicTime = record.getTimestamp();
            } else if ("Saturation".equals(recordType) &&
                      (recentSaturation == null || record.getTimestamp() > recentSaturationTime)) {
                recentSaturation = record.getMeasurementValue();
                recentSaturationTime = record.getTimestamp();
            }

            // Early termination if we found recent readings of both types
            if (recentSystolic != null && recentSaturation != null) {
                long timeDifference = Math.abs(recentSystolicTime - recentSaturationTime);
                if (timeDifference <= timeWindow) {
                    break;
                }
            }
        }

        if (recentSystolic != null && recentSaturation != null) {
            long timeDifference = Math.abs(recentSystolicTime - recentSaturationTime);
            if (timeDifference <= timeWindow &&
                recentSystolic < SYSTOLIC_LOWER &&
                recentSaturation < SATURATION_THRESHOLD) {

                hhFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia Alert",
                    Math.max(recentSystolicTime, recentSaturationTime)
                );
            }
        }
    }
}
