package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
// Braces style modified accordingly to google style guide and comments indented at same level as the code they describe
// changed class naming to UpperCamelCase

/**
 * {@code FileOutputStrategy} is an implementation of {@code OutputStrategy} that writes data into a file.
 *
 * <p>Data is formatted as: {@code patientId,timestamp,label,data}
 *
 */

public class FileOutputStrategy implements OutputStrategy {
    // Changed variable name to lowerCamelCase
    private String baseDirectory;
    // Changed name to fileMap since it's a regular field
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
    /**
     * Tries to write output to a new file
     *
     * @param patientId the unique ID of the patient
     * @param timestamp the time when the data was generated in milliseconds
     * @param label the type or category of health data (e.g., "ECG", "Blood Pressure")
     * @param data the actual health data in string format
     */

    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the filePath variable
        String filePath = fileMap.computeIfAbsent(label, k ->
                Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                    patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}