package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.factory.AlertFactory;
import com.alerts.factory.BloodOxygenAlertFactory;
import com.alerts.factory.BloodPressureAlertFactory;
import com.alerts.factory.ECGAlertFactory;

class AlertFactoryTest {

    @Test
    void shouldCreateBloodOxygenAlert() {
        // Arrange
        AlertFactory factory = new BloodOxygenAlertFactory();

        // Act & Assert
        assertAlertCreation(
                factory,
                "patient-123",
                "Low oxygen saturation (82%)",
                1_700_000_000_000L
        );
    }

    @Test
    void shouldCreateECGAlert() {
        // Arrange
        AlertFactory factory = new ECGAlertFactory();

        // Act & Assert
        assertAlertCreation(
                factory,
                "patient-673",
                "Abnormal heart rhythm detected",
                1_700_000_000_001L
        );
    }

    @Test
    void shouldCreateBloodPressureAlert() {
        // Arrange
        AlertFactory factory = new BloodPressureAlertFactory();

        // Act & Assert
        assertAlertCreation(
                factory,
                "patient-871",
                "Hypertensive alert (190/120 mmHg)",
                1_700_000_000_002L
        );
    }

    private void assertAlertCreation(AlertFactory factory,
                                     String expectedPatientId,
                                     String expectedCondition,
                                     long expectedTimestamp) {
        // Act
        Alert alert = factory.createAlert(
                expectedPatientId,
                expectedCondition,
                expectedTimestamp
        );

        // Assert
        assertNotNull(alert, "Alert should not be null");
        assertEquals(expectedPatientId, alert.getPatientId(),
                "Patient ID mismatch");
        assertEquals(expectedCondition, alert.getCondition(),
                "Condition mismatch");
        assertEquals(expectedTimestamp, alert.getTimestamp(),
                "Timestamp mismatch");

        // Verify concrete type
        assertTrue(alert.getClass().getSimpleName().contains(factory.getClass().getSimpleName()
                        .replace("Factory", "")),
                "Factory Alert mismatch: " + factory.getClass().getSimpleName());
    }
}
