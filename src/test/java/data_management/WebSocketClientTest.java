package data_management;

import com.data_management.DataStorage;
import com.data_management.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebSocketClientTest {
    private WebSocketClient client;
    private DataStorage mockStorage;

    @BeforeEach
    void setUp() throws URISyntaxException {
        mockStorage = mock(DataStorage.class);
        client = new WebSocketClient(new URI("ws://localhost:8080"), mockStorage);
    }

    @Test
    void testConnectionEstablishment() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(outContent)) {

            System.setOut(printStream);
            ServerHandshake handshake = mock(ServerHandshake.class);
            client.onOpen(handshake);

            assertTrue(outContent.toString().contains("Connected to server"));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void shouldProcessValidMessageFormat() {
        String validMessage = "25,1746370219657,ECG,-0.39264752241868395";
        client.onMessage(validMessage);
        verify(mockStorage).addPatientData(25, -0.39264752241868395, "ECG", 1746370219657L);
    }

    @Test
    void shouldIgnoreInvalidMessageFormat() {
        String invalidMessage = "Invalid message structure";
        client.onMessage(invalidMessage);
        verify(mockStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    void shouldHandleDataParsingErrors() {
        String corruptMessage = "not_number,1746370219657,M,12345.789";
        client.onMessage(corruptMessage);
        verify(mockStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    void shouldPropagateStorageExceptions() {
        doThrow(new RuntimeException("Storage failure")).when(mockStorage)
                .addPatientData(anyInt(), anyDouble(), anyString(), anyLong());

        Exception exception = assertThrows(RuntimeException.class, () ->
                client.onMessage("28,1746370222650,EEG,-0.18532655911156237"));

        assertEquals("Storage failure", exception.getMessage());
    }

    @Test
    void shouldCloseConnectionGracefully() {
        client.onClose(1000, "Normal closure", false);
        assertFalse(client.isOpen());
    }

    @Test
    void shouldHandleAbnormalClosure() {
        WebSocketClient spyClient = spy(client);
        doThrow(new RuntimeException("Connection reset")).when(spyClient)
                .onClose(anyInt(), anyString(), anyBoolean());

        assertThrows(RuntimeException.class, () ->
                spyClient.onClose(1006, "Abnormal closure", true));

        assertFalse(spyClient.isOpen());
    }

    @Test
    void shouldHandleNetworkErrors() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketClient spyClient = spy(client);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(spyClient).onClose(anyInt(), anyString(), anyBoolean());

        try (ByteArrayOutputStream errContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(errContent)) {

            System.setErr(printStream);
            spyClient.onError(new Exception("Network failure"));

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertTrue(errContent.toString().contains("Network failure"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void shouldEstablishConnectionWhenReadingData() throws Exception {
        WebSocketClient spyClient = spy(client);
        doNothing().when(spyClient).connect();

        spyClient.readData(mockStorage, new URI("ws://localhost:8080"));
        verify(spyClient).connect();
    }

    @Test
    void shouldHandleConnectionFailuresWhenReadingData() throws Exception {
        WebSocketClient spyClient = spy(client);
        doThrow(new RuntimeException("Connection timeout")).when(spyClient).connect();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        spyClient.readData(mockStorage, new URI("ws://localhost:8080"));
        assertTrue(errContent.toString().contains("Connection timeout"));
    }
}
