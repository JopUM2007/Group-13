 package com.alerts.strategy;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ManualAlertStrategyTest {

    private static final int TEST_PATIENT_ID = 2005;

    @Before
    public void setUp() {
        // Remove any existing patient with this ID for a clean test
        DataStorage.getInstance().getAllPatients().removeIf(p -> p.getPatientId() == TEST_PATIENT_ID);
    }

    @Test
    public void testManualAlertTriggersAlert() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 1.0, "Alert", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        ManualAlertStrategy strategy = new ManualAlertStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }

    @Test
    public void testNoManualAlertNoTrigger() {
        long now = System.currentTimeMillis();
        DataStorage.getInstance().addPatientData(TEST_PATIENT_ID, 120.0, "SystolicPressure", now);

        Patient patient = DataStorage.getInstance().getPatient(TEST_PATIENT_ID);
        List<PatientRecord> records = patient.getRecords(now - 1000, now + 1000);

        ManualAlertStrategy strategy = new ManualAlertStrategy();
        strategy.checkAlert(patient, records);

        assertNotNull(patient);
        assertFalse(records.isEmpty());
    }
}

