package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    // Constants for alert conditions
    private static final double SYSTOLIC_UPPER = 180.0;
    private static final double SYSTOLIC_LOWER = 90.0;
    private static final double DIASTOLIC_UPPER = 120.0;
    private static final double DIASTOLIC_LOWER = 60.0;
    private static final double BP_TREND_THRESHOLD = 10.0;
    private static final double SATURATION_THRESHOLD = 92.0;
    private static final double SATURATION_DROP = 5.0;
    private static final long RAPID_DROP_WINDOW_MS = 10 * 60 * 1000; //10 minutes in milliseconds
    private static final double ECG_PEAK_FACTOR = 2.0;
    private static final int ECG_WINDOW = 10;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        // Get patient records for evaluation - using the most recent 24 hours
        long currentTime = System.currentTimeMillis();
        long dayAgo = currentTime -(24*60*60*1000);
        List<PatientRecord> records = patient.getRecords(dayAgo, currentTime);

        if (records.isEmpty()) {
            return; // No records to evaluate
        }

        // Check for different alert conditions
        checkBloodPressureAlerts(patient, records);
        checkBloodPressureTrends(patient, records);
        checkSaturationAlerts(patient, records);
        checkSaturationRapidDrop(patient, records);
        checkHypotensiveHypoxemia(patient, records);
        checkECGAbnormalities(patient, records);
        checkManualAlert(patient, records);
    }

    /**
     * Check blood pressure readings against critical values and triggers alerts for systolic
     * or diastolic values that are outside the normal range.
     *
     * @param patient whose data is being examined
     * @param records the list of patient records to evaluate
     */
    private void checkBloodPressureAlerts(Patient patient, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            double value = record.getMeasurementValue();
            if (record.getRecordType().equals("SystolicPressure")) {
                if (value > SYSTOLIC_UPPER || value < SYSTOLIC_LOWER) {
                    triggerAlert(new Alert(
                            String.valueOf(patient.getPatientId()),
                            "Critical Systolic",
                            record.getTimestamp()
                    ));
                }
            } else if (record.getRecordType().equals("DiastolicPressure")) {
                if (value > DIASTOLIC_UPPER || value < DIASTOLIC_LOWER) {
                    triggerAlert(new Alert(
                            String.valueOf(patient.getPatientId()),
                            "Critical Diastolic",
                            record.getTimestamp()
                    ));
                }
            }
        }
    }

    /**
     * Check for critical trends in blood pressure over time by using systolic and
     * diastolic pressure changes.
     *
     * @param patient whose data is being examined
     * @param records the list of patient records to evaluate
     */
    private void checkBloodPressureTrends(Patient patient, List<PatientRecord> records) {
        checkTrend(patient, records, "SystolicPressure");
        checkTrend(patient, records, "DiastolicPressure");
    }

    /**
     * Helper method that evaluates a specific measure of blood pressure
     * at a consistent increasing or decreasing trend.
     *
     * @param patient whose data is being examined
     * @param records the list of patients records to evaluate
     * @param type of blood pressure measurement to check
     */
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
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Blood Pressure Trend Alert",
                        bpRecords.get(i + 2).getTimestamp()
                ));
            }
            // Decreasing Trend
            else if (v1 - v2 > BP_TREND_THRESHOLD && v2 - v3 > BP_TREND_THRESHOLD) {
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Blood Pressure Trend Alert",
                        bpRecords.get(i + 2).getTimestamp()
                ));
            }
        }
    }

    /**
     * Checks for blood oxygen saturation levels and triggers an alert when
     * a value is under the threshold.
     *
     * @param patient whose data is being examined
     * @param records the list of patient record to evaluate
     */
    private void checkSaturationAlerts(Patient patient, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("Saturation")) {
                double value = record.getMeasurementValue();
                if (value < SATURATION_THRESHOLD) {
                    triggerAlert(new Alert(
                            String.valueOf(patient.getPatientId()),
                            "Low Blood Saturation Alert",
                            record.getTimestamp()
                    ));
                }
            }
        }
    }

    /**
     * Check blood oxygen saturation rapid drops in a time window, which could indicate
     * respiratory distress.
     *
     * @param patient whose data is being examined
     * @param records the list of patient records to evaluate
     */
    private void checkSaturationRapidDrop(Patient patient, List<PatientRecord> records) {
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
                    triggerAlert(new Alert(
                            String.valueOf(patient.getPatientId()),
                            "Rapid Blood Saturation Drop Alert",
                            r2.getTimestamp()
                    ));
                    break;
                }
            }
        }
    }

    /**
     * Check for the combination of hypotension and hypoxemia occurring at the same time.
     *
     * @param patient whose data is being examined
     * @param records the list of patient records to evaluate
     */
    private void checkHypotensiveHypoxemia(Patient patient, List<PatientRecord> records) {
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
            triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Hypotensive Hypoxemia Alert",
                    Math.max(lastSystolicTime, lastSaturationTime)
            ));
        }
    }

    /**
     * Analyzes ECG to detect unusual peaks that diverge from the base of previous
     * readings.
     *
     * @param patient whose data is being examined
     * @param records the list of patient records to evaluate
     */
    private void checkECGAbnormalities(Patient patient, List<PatientRecord> records) {
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
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "ECG Abnormal Peak Alert",
                        ecgRecords.get(i).getTimestamp()
                ));
                break;
            }
        }
    }

    /**
     * Checks for any manually triggered alert that may have been entered by the staff.
     *
     * @param patient whose data is being examined
     * @param records the list of patient records to evaluate
     */
    private void checkManualAlert(Patient patient, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("Alert")) {
                triggerAlert(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Triggered Alert",
                        record.getTimestamp()
                ));
            }
        }
    }


    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    protected void triggerAlert(Alert alert) {
        System.out.printf(
                "[ALERT] Patient %s | Condition: %s | Timestamp: %d%n",
                alert.getPatientId(),
                alert.getCondition(),
                alert.getTimestamp()
        );
    }
}
