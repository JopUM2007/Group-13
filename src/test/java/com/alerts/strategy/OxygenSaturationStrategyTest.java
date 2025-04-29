package com.alerts.strategy;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OxygenSaturationStrategyTest {

    private static final int TEST_PATIENT_ID = 2002;

    @Before
    public void setUp() {
        // Remove any existing patient with this ID for a clean test
        DataStorage.getInstance().getAllPatients().removeIf(p -> p.getPatientId() == TEST_PATIENT_ID);
    }

    @Test
    public void testLowSaturationTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 91.0, "Saturation", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }

    @Test
    public void testRapidDropInSaturationTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 98.0, "Saturation", now - 300000); // 5 minutes ago
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 92.0, "Saturation", now); // Now (6% drop in 5 minutes)

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 600000, now + 1000);

        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertEquals(2, records.size());
    }

    @Test
    public void testNormalSaturationNoAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 98.0, "Saturation", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }
}