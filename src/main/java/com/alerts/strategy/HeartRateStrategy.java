package com.alerts.strategy;

import com.alerts.factory.ECGAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class HeartRateStrategy implements AlertStrategy {
    private static final double ECG_PEAK_FACTOR = 2.0;
    private static final int ECG_WINDOW = 10;
    private static final int BRADYCARDIA_THRESHOLD = 60;
    private static final int TACHYCARDIA_THRESHOLD = 100;
    private static final double IRREGULARITY_THRESHOLD = 0.2; // 20% variation

    ECGAlertFactory ecgFactory = new ECGAlertFactory();

    public void checkAlert(Patient patient, List<PatientRecord> records) {
        // Filter only ECG records once
        List<PatientRecord> ecgRecords = records.stream()
                .filter(r -> r.getRecordType().equals("ECG"))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .toList();

        for (PatientRecord record : ecgRecords) {
            double hr = record.getMeasurementValue();
            if (hr < BRADYCARDIA_THRESHOLD) {
                ecgFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Bradycardia Alert",
                        record.getTimestamp());
            } else if (hr > TACHYCARDIA_THRESHOLD) {
                ecgFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Tachycardia Alert",
                        record.getTimestamp());
            }
        }
        if (ecgRecords.size() >= 5) { // Minimum for rhythm analysis
            List<PatientRecord> window = ecgRecords.subList(
                    Math.max(0, ecgRecords.size()-5),
                    ecgRecords.size()
            );
            if (isIrregular(window)) {
                ecgFactory.createAlert(
                        String.valueOf(patient.getPatientId()),
                        "Irregular Rhythm Alert",
                        window.get(window.size()-1).getTimestamp()
                );
            }
        }

        if (ecgRecords.size() <= ECG_WINDOW) return;

        // Calculate initial window sum to avoid redundant calculations
        double windowSum = 0;
        for (int j = 0; j < ECG_WINDOW; j++) {
            windowSum += ecgRecords.get(j).getMeasurementValue();
        }

        // Use sliding window approach to avoid recalculating the entire sum
        for (int i = ECG_WINDOW; i < ecgRecords.size(); i++) {
            double avg = windowSum / ECG_WINDOW;
            double val = ecgRecords.get(i).getMeasurementValue();

            if (val > avg * ECG_PEAK_FACTOR) {
                ecgFactory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "ECG Abnormal Peak Alert",
                    ecgRecords.get(i).getTimestamp()
                );
                break;
            }
            // Update window: remove oldest, add newest
            windowSum -= ecgRecords.get(i - ECG_WINDOW).getMeasurementValue();
            windowSum += val;
        }
    }
    private boolean isIrregular(List<PatientRecord> window) {
        double[] intervals = new double[window.size()-1];
        for (int i=1; i<window.size(); i++) {
            intervals[i-1] = window.get(i).getTimestamp() - window.get(i-1).getTimestamp();
        }
        double avg = Arrays.stream(intervals).average().orElse(0);
        return Arrays.stream(intervals)
                .anyMatch(interval -> Math.abs(interval - avg)/avg >= IRREGULARITY_THRESHOLD);
    }
}
