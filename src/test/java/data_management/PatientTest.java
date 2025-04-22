package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PatientTest {
    @Test
    void testGetRecordsWithinRange() {
        Patient patient = new Patient(1);
        patient.addRecord(120.0, "SystolicPressure", 1000L);
        patient.addRecord(80.0, "DiastolicPressure", 2000L);
        patient.addRecord(95.0, "Saturation", 3000L);

        List<PatientRecord> result = patient.getRecords(1500L, 3500L);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getRecordType().equals("DiastolicPressure")));
        assertTrue(result.stream().anyMatch(r -> r.getRecordType().equals("Saturation")));
    }

    @Test
    void testGetRecordsEmptyWhenNoMatch() {
        Patient patient = new Patient(1);
        patient.addRecord(120.0, "SystolicPressure", 1000L);

        List<PatientRecord> result = patient.getRecords(2000L, 3000L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRecordsEdgeCases() {
        Patient patient = new Patient(1);
        patient.addRecord(120.0, "SystolicPressure", 1000L);
        patient.addRecord(80.0, "DiastolicPressure", 2000L);

        // Start and end exactly at record timestamps
        List<PatientRecord> result = patient.getRecords(1000L, 2000L);
        assertEquals(2, result.size());
    }
}

