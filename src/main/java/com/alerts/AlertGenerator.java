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
    private final List<AlertStrategy> strategies;

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

        this.strategies = List.of(
                new BloodPressureStrategy(),
                new OxygenSaturationStrategy(),
                new HeartRateStrategy(),
                new HypotensiveHypoxemiaStrategy(),
                new ManualAlertStrategy()
        ).stream().map(AlertStrategy.class::cast).toList();
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
        System.out.println("Evaluating data for patient: " + patient.getPatientId() + " ==> ");

        // Get the recent records for this patient
        List<PatientRecord> records = dataStorage.getRecords(
                patient.getPatientId(),
                System.currentTimeMillis() - 24*60*60*1000, // last 24 hours
                System.currentTimeMillis()
        );

        // Apply each strategy to all patient records at once
        for (AlertStrategy strategy : strategies) {
            strategy.checkAlert(patient, records);
        }

        System.out.println(); // Add a blank line after processing
    }
}
