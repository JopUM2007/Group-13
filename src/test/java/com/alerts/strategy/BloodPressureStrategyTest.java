package com.alerts.strategy;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BloodPressureStrategyTest {

    private static final int TEST_PATIENT_ID = 2001;

    @Before
    public void setUp() {
        // Remove any existing patient with this ID for a clean test
        DataStorage.getInstance().getAllPatients().removeIf(p -> p.getPatientId() == TEST_PATIENT_ID);
    }

    @Test
    public void testCriticalSystolicTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 185, "SystolicPressure", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }

    @Test
    public void testCriticalDiastolicTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 125, "DiastolicPressure", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }

    @Test
    public void testBloodPressureTrendTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 100, "SystolicPressure", now);
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 120, "SystolicPressure", now + 1000);
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 140, "SystolicPressure", now + 2000);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 3000);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertEquals(3, records.size());
    }
}
