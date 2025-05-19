package com.alerts;

import com.alerts.strategy.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.stream.Stream;

/**
 * The AlertGenerator class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a  DataStorage instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;
    private final List<AlertStrategy> strategies;

    /**
     * Constructs an AlertGenerator with a specified  DataStorage.
     * The DataStorage is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient data.
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;

        this.strategies = Stream.of(
                new BloodPressureStrategy(),
                new OxygenSaturationStrategy(),
                new HeartRateStrategy(),
                new HypotensiveHypoxemiaStrategy(),
                new ManualAlertStrategy()
        ).toList();
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
      //  System.out.println("Evaluating data for patient: " + patient.getPatientId() + " ==> ");

        // Get the recent records for this patient for the last 24 hours
        List<PatientRecord> records = dataStorage.getRecords(
                patient.getPatientId(),
                System.currentTimeMillis() - 24*60*60*1000,
                System.currentTimeMillis()
        );

        for (AlertStrategy strategy : strategies) {
            strategy.checkAlert(patient, records);
        }

    }
}
