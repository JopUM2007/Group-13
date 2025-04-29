package com.alerts;

import com.alerts.strategy.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;



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
        new BloodPressureStrategy().checkAlert(patient, records);
        new OxygenSaturationStrategy().checkAlert(patient, records);
        new HeartRateStrategy().checkAlert(patient, records);
        new HypotensiveHypoxemiaStrategy().checkAlert(patient, records);
        new ManualAlertStrategy().checkAlert(patient, records);
    }
}
