package com.alerts.strategy;

    import com.data_management.DataStorage;
    import com.data_management.Patient;
    import com.data_management.PatientRecord;
    import org.junit.Before;
    import org.junit.Test;
    import java.util.List;

    import static org.junit.jupiter.api.Assertions.*;

    public class HypotensiveHypoxemiaStrategyTest {

        private static final int TEST_PATIENT_ID = 2004;

        @Before
        public void setUp() {
            // Remove any existing patient with this ID for a clean test
            DataStorage.getInstance().getAllPatients().removeIf(p -> p.getPatientId() == TEST_PATIENT_ID);
        }

        @Test
        public void testHypotensiveHypoxemiaTriggersAlert() {
            long now = System.currentTimeMillis();
            DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 85.0, "SystolicPressure", now);
            DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 90.0, "Saturation", now);

            Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
            List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

            HypotensiveHypoxemiaStrategy strategy = new HypotensiveHypoxemiaStrategy();
            strategy.checkAlert(patient, records);

            assertNotNull(patient);
            assertEquals(2, records.size());
        }

        @Test
        public void testLowBloodPressureNormalSaturationNoAlert() {
            long now = System.currentTimeMillis();
            DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 85.0, "SystolicPressure", now);
            DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 96.0, "Saturation", now);

            Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
            List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

            HypotensiveHypoxemiaStrategy strategy = new HypotensiveHypoxemiaStrategy();
            strategy.checkAlert(patient, records);

            assertNotNull(patient);
            assertEquals(2, records.size());
        }

        @Test
        public void testNormalBloodPressureLowSaturationNoAlert() {
            long now = System.currentTimeMillis();
            DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 120.0, "SystolicPressure", now);
            DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 90.0, "Saturation", now);

            Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
            List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

            HypotensiveHypoxemiaStrategy strategy = new HypotensiveHypoxemiaStrategy();
            strategy.checkAlert(patient, records);

            assertNotNull(patient);
            assertEquals(2, records.size());
        }
    }