package data_management;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertGeneratorTest {

    private DataStorage mockDataStorage;
    private AlertGenerator alertGenerator;
    private ByteArrayOutputStream outputCapture;
    private PrintStream originalOut;
    private long currentTimestamp;

    @BeforeEach
    void initializeTestEnvironment() {
        outputCapture = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputCapture));

        // Print something to verify output capture is working
        System.out.println("Test output capture");

        mockDataStorage = mock(DataStorage.class);
        alertGenerator = new AlertGenerator(mockDataStorage);
        currentTimestamp = System.currentTimeMillis();
    }


    @AfterEach
    void restoreSystemOutput() {
        System.setOut(originalOut);
    }


    private Patient createTestPatient(int patientId) {
        return new Patient(patientId);
    }

    private void mockPatientRecords(List<PatientRecord> records) {
        when(mockDataStorage.getRecords(anyInt(), anyLong(), anyLong()))
                .thenReturn(records);
    }

        @Test
        void shouldTriggerCriticalSystolicAlertWhenValueExceedsThreshold() {
            // Arrange
            Patient patient = createTestPatient(1);
            mockPatientRecords(Collections.singletonList(
                    new PatientRecord(1, 200, "SystolicPressure", currentTimestamp)
            ));

            // Act
            alertGenerator.evaluateData(patient);

            // Assert
            assertConsoleOutputContains("Critical Systolic");
        }

        @Test
        void shouldTriggerCriticalDiastolicAlertWhenValueBelowThreshold() {
            // Arrange
            Patient patient = createTestPatient(1);
            mockPatientRecords(Collections.singletonList(
                    new PatientRecord(1, 10, "DiastolicPressure", currentTimestamp)
            ));

            // Act
            alertGenerator.evaluateData(patient);

            // Assert
            assertConsoleOutputContains("Critical Diastolic");
        }

        @Test
        void shouldDetectIncreasingTrendInSystolicReadings() {
            // Arrange
            Patient patient = createTestPatient(1);
            mockPatientRecords(List.of(
                    // Timestamps within 24h window, 2h gaps
                    new PatientRecord(1, 110, "SystolicPressure", currentTimestamp - 7_200_000), // 2h ago
                    new PatientRecord(1, 121, "SystolicPressure", currentTimestamp - 3_600_000), // 1h ago
                    new PatientRecord(1, 132, "SystolicPressure", currentTimestamp)
            ));

            // Act
            alertGenerator.evaluateData(patient);

            // Assert
            assertConsoleOutputContains("Systolic Pressure Increasing");
        }

        @Test
        void shouldDetectDecreasingTrendInSystolicReadings() {
            // Arrange
            Patient patient = createTestPatient(1);
            mockPatientRecords(List.of(
                    // Decreasing values with valid timestamps
                    new PatientRecord(1, 130, "SystolicPressure", currentTimestamp - 7_200_000),
                    new PatientRecord(1, 119, "SystolicPressure", currentTimestamp - 3_600_000),
                    new PatientRecord(1, 108, "SystolicPressure", currentTimestamp)
            ));

            // Act
            alertGenerator.evaluateData(patient);

            // Assert
            assertConsoleOutputContains("Systolic Pressure Decreasing");
        }


        @Test
        void shouldTriggerLowSaturationAlertWhenBelowThreshold() {
            // Arrange
            Patient patient = createTestPatient(1);
            mockPatientRecords(Collections.singletonList(
                    new PatientRecord(1, 85, "Saturation", currentTimestamp)
            ));

            // Act
            alertGenerator.evaluateData(patient);

            // Assert
            assertConsoleOutputContains("Low Blood Saturation Alert");
        }

        @Test
        void shouldDetectRapidOxygenDrop() {
            // Arrange
            Patient patient = createTestPatient(1);
            mockPatientRecords(List.of(
                    new PatientRecord(1, 100, "Saturation", currentTimestamp - 600000), // 10 minutes ago
                    new PatientRecord(1, 93, "Saturation", currentTimestamp)
            ));

            // Act
            alertGenerator.evaluateData(patient);

            // Assert
            assertConsoleOutputContains("Rapid Blood Saturation Drop Alert");
        }


        @Test
        void shouldTriggerBradycardiaAlert() {
            Patient patient = createTestPatient(1);
            // Single reading <60 bpm
            mockPatientRecords(List.of(
                    new PatientRecord(1, 55, "ECG", currentTimestamp)
            ));
            alertGenerator.evaluateData(patient);
            assertConsoleOutputContains("Bradycardia Alert");
        }

    @Test
    void shouldDetectIrregularRhythm() {
        Patient patient = createTestPatient(1);
        List<PatientRecord> records = new ArrayList<>();
        long time = currentTimestamp;

        // Alternating intervals: 800ms and 1200ms (20% variation)
        for (int i=0; i<15; i++) {
            records.add(new PatientRecord(1, i%2==0 ? 90 : 60, "ECG", time));
            time += (i%2 == 0) ? 800 : 1200; // Alternating short/long intervals
        }

        mockPatientRecords(records);
        alertGenerator.evaluateData(patient);
        assertConsoleOutputContains("Irregular Rhythm Alert");
    }

    @Test
    void shouldTriggerHypotensiveHypoxemiaAlertWhenBothConditionsMet() {
        // Arrange
        Patient patient = createTestPatient(1);
        mockPatientRecords(List.of(
                new PatientRecord(1, 89, "SystolicPressure", currentTimestamp),
                new PatientRecord(1, 91, "Saturation", currentTimestamp)
        ));

        // Act
        alertGenerator.evaluateData(patient);

        // Assert
        assertConsoleOutputContains("Hypotensive Hypoxemia Alert");
    }

    private void assertConsoleOutputContains(String expected) {
        String output = outputCapture.toString().trim();
        assertTrue(output.contains(expected),
                "Expected output to contain: " + expected + "\nActual output: " + output);
    }
}
