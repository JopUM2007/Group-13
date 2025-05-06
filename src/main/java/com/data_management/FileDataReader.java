

package com.data_management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

public class FileDataReader implements DataReader {

    private String directoryPath;

    public FileDataReader(String directoryPath) {this.directoryPath = directoryPath;}

    @Override
    public void readData(DataStorage dataStorage, URI servies) throws IOException {
        // Construct the file path based on the directoryPath
        String filePath = directoryPath + "/output.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse each line and extract relevant data
                String[] parts = line.split(",");
                int patientId = Integer.parseInt(parts[0]);
                double measurementValue = Double.parseDouble(parts[1]);
                String recordType = parts[2];
                long timestamp = Long.parseLong(parts[3]);

                // Add the parsed data to the DataStorage
                dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
            }
        }
    }
}
