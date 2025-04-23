package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;

class FileDataReaderTest {

    @TempDir
    Path tempDir; // Creates a temporary directory for test files

    @Test
    void testReadDataFromFile() throws IOException {
        // 1. Create a test file with sample data
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, List.of(
                "Patient ID: 1, Timestamp: 1714376789050, Label: WhiteBloodCells, Data: 100.0",
                "Patient ID: 1, Timestamp: 1714376789051, Label: WhiteBloodCells, Data: 200.0"
        ));

        // 2. Read the file using FileDataReader
        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        // 3. Verify data was loaded correctly
        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals(200.0, records.get(1).getMeasurementValue());
    }
}

