package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating patient health data.
 *
 * <p>Classes implementing this interface are responsable for generating specific
 * types of health data for patients and outputting it through the provided strategy
 */
public interface PatientDataGenerator {

    /**
     * Generates health data for a specific patient.
     *
     * @param patientId the unique ID of the patient for whom data is being generated
     * @param outputStrategy the strategy to use for outputting the data generated
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
