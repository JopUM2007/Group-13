package com.data_management;

import java.io.IOException;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the DataReader interface to read patient data from text files.
 * The class parses text files containing patient records in a given format
 * and stores them in a DataStorage instance.
 */
public class FileDataReader implements DataReader {
    private final Path outputDirectory;

    public FileDataReader(String outputDir) {
        this.outputDirectory = Paths.get(outputDir);
    }

    /**
     * Reads all .txt files in a directory and processes them as patient records.
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDirectory, "*.txt")) {
            for (Path file : stream) {
                processFile(file, dataStorage);
            }
        }
    }

    /**
     * Process an individual file and extracts patient records.
     *
     * @param filePath the path to the file to process
     * @param dataStorage the data storage where the parsed data will be stored
     * @throws IOException if an I/O error occurs while reading the file
     */
    private void processFile(Path filePath, DataStorage dataStorage) throws IOException {
        Pattern pattern = Pattern.compile(
                "Patient ID: (\\d+), Timestamp: (\\d+), Label: (\\w+), Data: ([-+]?\\d*\\.?\\d+)"
        );

        Files.lines(filePath).forEach(line -> {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                try {
                    int patientId = Integer.parseInt(matcher.group(1));
                    long timestamp = Long.parseLong(matcher.group(2));
                    String label = matcher.group(3);
                    double data = Double.parseDouble(matcher.group(4));

                    dataStorage.addPatientData(patientId, data, label, timestamp);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing line: " + line);
                }
            }
        });
    }
}

