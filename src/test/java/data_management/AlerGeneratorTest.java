package data_management;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class AlertGeneratorTest {
    static class TestAlertGenerator extends AlertGenerator {
        List<Alert> triggered = new ArrayList<>();
        TestAlertGenerator(DataStorage ds) { super(ds); }
        @Override
        protected void triggerAlert(Alert alert) {
            // Add the alert to our list instead of just printing it
            triggered.add(alert);
            // Optionally call the parent method as well if you want to keep the console output
            super.triggerAlert(alert);
        }
    }

    @Test
    void testCriticalSystolicAlert() {
        DataStorage ds = new DataStorage();
        long timestamp = System.currentTimeMillis();
        ds.addPatientData(1, 185.0, "SystolicPressure", timestamp);

        Patient patient = ds.getPatient(1);
        TestAlertGenerator ag = new TestAlertGenerator(ds);
        ag.evaluateData(patient);

        assertTrue(ag.triggered.stream().anyMatch(a -> a.getCondition().contains("Critical Systolic")));
    }

    @Test
    void testBloodPressureTrendAlert() {
        DataStorage ds = new DataStorage();
        long now = System.currentTimeMillis();

        // Increasing trend (120 -> 131 -> 142)
        ds.addPatientData(1, 120.0, "SystolicPressure", now - 2000);
        ds.addPatientData(1, 131.0, "SystolicPressure", now - 1000);
        ds.addPatientData(1, 142.0, "SystolicPressure", now);

        Patient patient = ds.getPatient(1);
        TestAlertGenerator ag = new TestAlertGenerator(ds);
        ag.evaluateData(patient);

        assertTrue(ag.triggered.stream().anyMatch(a ->
                a.getCondition().contains("Blood Pressure Trend Alert")
        ));
    }

    @Test
    void testLowSaturationAlert() {
        DataStorage ds = new DataStorage();
        long timestamp = System.currentTimeMillis();
        ds.addPatientData(1, 91.0, "Saturation", timestamp);
        Patient patient = ds.getPatient(1);
        TestAlertGenerator ag = new TestAlertGenerator(ds);
        ag.evaluateData(patient);
        assertTrue(ag.triggered.stream().anyMatch(a -> a.getCondition().contains("Saturation")));
    }

    @Test
    void testRapidDropSaturationAlert() {
        DataStorage ds = new DataStorage();
        long timestamp = System.currentTimeMillis();
        long baseTime = timestamp - (15 * 60 * 1000);

        ds.addPatientData(1, 97.0, "Saturation", baseTime - (10 * 60 * 1000));
        ds.addPatientData(1, 91.0, "Saturation", baseTime - (5 * 60 * 1000)); // drop by 6 in 5 min

        Patient patient = ds.getPatient(1);
        TestAlertGenerator ag = new TestAlertGenerator(ds);
        ag.evaluateData(patient);

        assertTrue(ag.triggered.stream().anyMatch(a -> a.getCondition().contains("Rapid Blood Saturation Drop Alert")));
    }

    @Test
    void testHypotensiveHypoxemiaAlert() {
        DataStorage ds = new DataStorage();
        long timestamp = System.currentTimeMillis();
        ds.addPatientData(1, 85.0, "SystolicPressure", timestamp);
        ds.addPatientData(1, 90.0, "Saturation", timestamp);
        Patient patient = ds.getPatient(1);
        TestAlertGenerator ag = new TestAlertGenerator(ds);
        ag.evaluateData(patient);
        assertTrue(ag.triggered.stream().anyMatch(a -> a.getCondition().contains("Hypotensive")));
    }

    @Test
    void testECGAbnormalPeakAlert() {
        DataStorage ds = new DataStorage();
        long timestamp = System.currentTimeMillis();
        long baseTime = timestamp    - 30_000L;
        // Add 10 normal ECG readings
        for (int i = 0; i < 10; i++) ds.addPatientData(1, 1.0, "ECG", baseTime + i * 1000);
        // Add a peak
        ds.addPatientData(1, 3.0, "ECG", baseTime + 20000L);
        Patient patient = ds.getPatient(1);
        TestAlertGenerator ag = new TestAlertGenerator(ds);
        ag.evaluateData(patient);
        assertTrue(ag.triggered.stream().anyMatch(a -> a.getCondition().contains("ECG Abnormal Peak Alert")));
    }

    @Test
    void testManualTriggeredAlert() {
        DataStorage ds = new DataStorage();
        long timestamp = System.currentTimeMillis();
        ds.addPatientData(1, 0.0, "Alert", timestamp);
        Patient patient = ds.getPatient(1);
        TestAlertGenerator ag = new TestAlertGenerator(ds);
        ag.evaluateData(patient);
        assertTrue(ag.triggered.stream().anyMatch(a -> a.getCondition().contains("Triggered")));
    }
}

