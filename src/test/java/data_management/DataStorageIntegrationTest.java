package data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

class DataStorageIntegrationTest {

    private DataStorage storage;

    @BeforeEach
    void initializeStorage() {
        storage = DataStorage.getInstance();
        clearStorageData();
    }

    private void clearStorageData() {
        storage.getAllPatients().forEach(patient ->
                storage.getRecords(patient.getPatientId(), 0, Long.MAX_VALUE).clear()
        );
    }

    @Test
    void shouldStoreAndRetrievePatientRecordsWithCorrectValues() {
        // Arrange
        int patientId = 123;
        String recordType = "WhiteBloodCells";
        long timestamp1 = 1714376789050L;
        long timestamp2 = 1714376789051L;

        // Act
        storage.addPatientData(patientId, 100.0, recordType, timestamp1);
        storage.addPatientData(patientId, 200.0, recordType, timestamp2);

        // Assert
        List<PatientRecord> records = storage.getRecords(patientId, timestamp1, timestamp2);

        assertEquals(2, records.size(),
                "Should retrieve exactly two records in time range");

        verifyRecord(records.get(0), patientId, 100.0, recordType, timestamp1);
        verifyRecord(records.get(1), patientId, 200.0, recordType, timestamp2);
    }

    private void verifyRecord(PatientRecord record,
                              int expectedPatientId,
                              double expectedValue,
                              String expectedType,
                              long expectedTimestamp) {
        assertEquals(expectedPatientId, record.getPatientId(),
                "Patient ID mismatch");
        assertEquals(expectedType, record.getRecordType(),
                "Record mismatch");
        assertEquals(expectedValue, record.getMeasurementValue(), 0.001,
                "Tolerance exceeded");
        assertEquals(expectedTimestamp, record.getTimestamp(),
                "Timestamp mismatch");
    }
}
