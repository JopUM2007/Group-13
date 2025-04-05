package com.cardio_generator.outputs;

/**
 * Strategy interface for outputting patient health data.
 *
 * <p>The interface provides a strategy for different output mechanisms
 * that can be used to handle patient health data.
 */
public interface OutputStrategy {

    /**
     * Outputs patient health data.
     *
     * @param patientId the unique ID of the patient
     * @param timestamp the time when the data was generated in milliseconds
     * @param label the type or category of health data (e.g., "ECG", "Blood Pressure")
     * @param data the actual health data in string format
     */
    void output(int patientId, long timestamp, String label, String data);
}
