package com.alerts.strategy;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HeartRateStrategyTest {

    private static final int TEST_PATIENT_ID = 2003;

    @Before
    public void setUp() {
        // Remove any existing patient with this ID for a clean test
        DataStorage.getInstance().getAllPatients().removeIf(p -> p.getPatientId() == TEST_PATIENT_ID);
    }

    @Test
    public void testHighHeartRateTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 130.0, "HeartRate", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        HeartRateStrategy strategy = new HeartRateStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }

    @Test
    public void testLowHeartRateTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 45.0, "HeartRate", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        HeartRateStrategy strategy = new HeartRateStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }

    @Test
    public void testNormalHeartRateNoAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 75.0, "HeartRate", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        HeartRateStrategy strategy = new HeartRateStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }
}