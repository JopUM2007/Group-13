package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;
    /**
     * {@code AlertGenerator} is an implementation of the {@code PatientDataGenerator} interface  that generates a random AlertState
     * for each patient which it then generates data with.
     *
     * <p>Data is formatted as: {@code patientId},{@code current time}, {@code label}, {@code data}
     */
public class AlertGenerator implements PatientDataGenerator {

    //Changed constant name in ALL_CAPS_WITH_UNDERSCORES
    public static final Random RANDOM_GENERATOR = new Random();
    // Changed variable name to lowerCamelCase
    private boolean[] alertStates; // false = resolved, true = pressed

    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }
        /**
         * Prints out the data
         *
         * @param patientId      the unique ID of the patient
         * @param outputStrategy the set strategy to be used
         */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Changed variable name to lowerCamelCase
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}