package data_management;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DataStorageTest {

    private static final int TEST_PATIENT_ID = 3001;
    private DataStorage storage;

    @Before
    public void setUp() {
        storage = DataStorage.getInstance();
        // Clean up any existing test patient
        storage.getAllPatients().removeIf(p -> p.getPatientId() == TEST_PATIENT_ID);
    }

    @Test
    public void testAddAndRetrievePatientData() {
        long timestamp = System.currentTimeMillis();
        storage.addPatientData(TEST_PATIENT_ID, 120.0, "SystolicPressure", timestamp);

        // Verify patient was created
        Patient patient = storage.getPatient(TEST_PATIENT_ID);
        assertNotNull(patient);
        assertEquals(TEST_PATIENT_ID, patient.getPatientId());

        // Verify record was added
        List<PatientRecord> records = storage.getRecords(TEST_PATIENT_ID, timestamp - 1000, timestamp + 1000);
        assertEquals(1, records.size());
        assertEquals(120.0, records.get(0).getMeasurementValue());
        assertEquals("SystolicPressure", records.get(0).getRecordType());
        assertEquals(timestamp, records.get(0).getTimestamp());
    }

    @Test
    public void testAddMultipleRecordsForPatient() {
        long timestamp = System.currentTimeMillis();
        storage.addPatientData(TEST_PATIENT_ID, 120.0, "SystolicPressure", timestamp);
        storage.addPatientData(TEST_PATIENT_ID, 80.0, "DiastolicPressure", timestamp + 1000);
        storage.addPatientData(TEST_PATIENT_ID, 72.0, "HeartRate", timestamp + 2000);

        List<PatientRecord> records = storage.getRecords(TEST_PATIENT_ID, timestamp - 1000, timestamp + 3000);
        assertEquals(3, records.size());
    }

    @Test
    public void testRecordTimeFiltering() {
        long timestamp = System.currentTimeMillis();
        storage.addPatientData(TEST_PATIENT_ID, 120.0, "SystolicPressure", timestamp);
        storage.addPatientData(TEST_PATIENT_ID, 80.0, "DiastolicPressure", timestamp + 60000); // 1 minute later
        storage.addPatientData(TEST_PATIENT_ID, 72.0, "HeartRate", timestamp + 120000); // 2 minutes later

        // Get only the first record
        List<PatientRecord> records = storage.getRecords(TEST_PATIENT_ID, timestamp - 1000, timestamp + 1000);
        assertEquals(1, records.size());
        assertEquals("SystolicPressure", records.get(0).getRecordType());

        // Get the first and second records
        records = storage.getRecords(TEST_PATIENT_ID, timestamp - 1000, timestamp + 61000);
        assertEquals(2, records.size());

        // Get all records
        records = storage.getRecords(TEST_PATIENT_ID, timestamp - 1000, timestamp + 130000);
        assertEquals(3, records.size());
    }

    @Test
    public void testGetNonexistentPatient() {
        // Try to get records for a patient that doesn't exist
        List<PatientRecord> records = storage.getRecords(999999, 0, Long.MAX_VALUE);
        assertNotNull(records);
        assertTrue(records.isEmpty());

        // Verify the patient doesn't exist
        Patient patient = storage.getPatient(999999);
        assertNull(patient);
    }

    @Test
    public void testGetAllPatientsMethod() {
        // Clear any existing patients with our test IDs
        storage.getAllPatients().removeIf(p -> p.getPatientId() >= TEST_PATIENT_ID &&
                                              p.getPatientId() < TEST_PATIENT_ID + 10);

        // Add some test patients
        storage.addPatientData(TEST_PATIENT_ID, 120.0, "SystolicPressure", System.currentTimeMillis());
        storage.addPatientData(TEST_PATIENT_ID + 1, 118.0, "SystolicPressure", System.currentTimeMillis());
        storage.addPatientData(TEST_PATIENT_ID + 2, 122.0, "SystolicPressure", System.currentTimeMillis());

        // Get all patients
        List<Patient> patients = storage.getAllPatients();

        // Verify we have at least our 3 test patients
        assertTrue(patients.size() >= 3);

        // Verify our test patients exist in the returned list
        assertTrue(patients.stream().anyMatch(p -> p.getPatientId() == TEST_PATIENT_ID));
        assertTrue(patients.stream().anyMatch(p -> p.getPatientId() == TEST_PATIENT_ID + 1));
        assertTrue(patients.stream().anyMatch(p -> p.getPatientId() == TEST_PATIENT_ID + 2));
    }

    @Test
    public void testSingletonInstance() {
        // Test that we always get the same instance
        DataStorage instance1 = DataStorage.getInstance();
        DataStorage instance2 = DataStorage.getInstance();
        assertSame(instance1, instance2);
        assertSame(storage, instance1);
    }
}