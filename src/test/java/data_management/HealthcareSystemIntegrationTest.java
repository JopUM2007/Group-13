package data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClient;

class HealthcareSystemIntegrationTest {

    private DataStorage storage;
    private AlertGenerator alertGenerator;
    private WebSocketClient webSocketClient;
    private ByteArrayOutputStream outputCapture;
    private PrintStream originalOutput;
    private long testStartTime;

    @BeforeEach
    void initializeTestEnvironment() throws URISyntaxException {
        storage = DataStorage.getInstance();
        alertGenerator = spy(new AlertGenerator(storage));
        webSocketClient = spy(new WebSocketClient(new URI("ws://localhost:8080"), storage));
        outputCapture = new ByteArrayOutputStream();
        originalOutput = System.out;
        System.setOut(new PrintStream(outputCapture));
        testStartTime = System.currentTimeMillis();
    }

    @AfterEach
    void cleanupTestEnvironment() {
        System.setOut(originalOutput);
    }

    @Test
    void shouldTriggerAlertWhenReceivingCriticalData() throws Exception {
        // Arrange
        doNothing().when(webSocketClient).connect();
        webSocketClient.onOpen(mock(ServerHandshake.class));
        final long timestamp = testStartTime - 10_000;
        final int patientId = 19;
        // Use a lower value for oxygen saturation to ensure it triggers an alert
        final String testMessage = patientId + "," + timestamp + ",Saturation,85";

        // Act
        webSocketClient.onMessage(testMessage);

        // Assert
        List<Patient> patients = storage.getAllPatients();
        assertFalse(patients.isEmpty(), "No patients found");
        assertEquals(patientId, patients.get(0).getPatientId(), "Patient Id mismatch");

        List<PatientRecord> records = storage.getRecords(patientId, timestamp, timestamp);
        assertFalse(records.isEmpty(), "No records found for this patient");
        assertEquals(1, records.size(), "Unexpected number of records");
        assertEquals("Saturation", records.get(0).getRecordType(), "Record type mismatch");
        assertEquals(85, records.get(0).getMeasurementValue(), 0.001, "Measurement value mismatch");

        // Reset the output stream before evaluating
        outputCapture.reset();

        // Evaluate data and check for alert
        alertGenerator.evaluateData(patients.get(0));

        String output = outputCapture.toString();
        System.setOut(originalOutput);
        System.out.println("Actual alert output: " + output);

        // Check for partial match if exact format is unknown
        boolean containsLowSaturationAlert = output.toLowerCase().contains("saturation") &&
                                            output.toLowerCase().contains("low");
        assertTrue(containsLowSaturationAlert, "Expected low saturation alert not triggered");
    }

    @Test
    void shouldEstablishConnectionWhenReadingData() throws Exception {
        // Arrange
        WebSocketClient webSocketClientSpy = spy(webSocketClient);

        // Act
        doNothing().when(webSocketClientSpy).connect();
        webSocketClientSpy.readData(storage, new URI("ws://localhost:8080"));

        // Assert
        verify(webSocketClientSpy, times(1))
                .connect();
    }
}
