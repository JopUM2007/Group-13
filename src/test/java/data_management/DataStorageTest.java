package data_management;

import com.data_management.PatientRecord;
import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {
    @Test
    void testAddAndGetRecords() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1000L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 2000L);

        List<PatientRecord> records = storage.getRecords(1, 900L, 2100L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals(200.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecordsForNonexistentPatient() {
        DataStorage storage = new DataStorage();
        List<PatientRecord> records = storage.getRecords(99, 0, 10000);
        assertTrue(records.isEmpty());
    }
}

