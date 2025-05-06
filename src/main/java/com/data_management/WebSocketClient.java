package com.data_management;

import java.io.IOException;
import java.net.URI;

import org.java_websocket.handshake.ServerHandshake;

/**
 * WebSocketClient connects to a server and reads data.
 * It implements the DataReader interface and uses a DataStorage instance to store received data.
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient implements DataReader {
    private DataStorage dataStorage;

    /**
     * Constructs a new TheWebSocketClient with the specified server URI and DataStorage.
     *
     * @param serverUri    the URI of the server to connect to
     * @param dataStorage  the DataStorage instance to use for storing data
     */
    public WebSocketClient(URI serverUri, DataStorage dataStorage) {
        super(serverUri);
        this.dataStorage = dataStorage;
    }

    /**
     * Called when the WebSocket connection is opened.
     *
     * @param handshakedata  the server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    /**
     * Called when a message is received from the server.
     *
     * @param message  the message received from the server
     */
    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        try {
            processMessage(message);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid message: " + e.getMessage());
        }
    }

    /**
     * Processes a message received from the WebSocket server.
     * The message is expected to be in the format: "patientId,timestamp,label,data"
     * where patientId is an integer, timestamp is a long, label is a string,
     * and data is a double value.
     *
     * @param message the raw message string received from the WebSocket server
     * @throws IllegalArgumentException if the message format is invalid or cannot be parsed
     * @throws RuntimeException if there's an error storing the data in the data storage
     */
    private void processMessage(String message) {
        String[] parts = message.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid message");
        }
        try {
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label = parts[2].trim();
            double data = Double.parseDouble(parts[3].trim());
            dataStorage.addPatientData(patientId, data, label, timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in message: " + message, e);
        }
    }

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code     the closure code
     * @param reason   the reason for closure
     * @param remote   whether the closure was initiated by the remote host
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with exit code " + code + " additional info: " + reason);
    }

    /**
     * Called when an error occurs.
     *
     * @param ex  the exception representing the error
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred: " + ex.getMessage());
        onClose(1001, "Error: " + ex.getMessage(), false); // Trigger onClose on error
    }

    /**
     * Reads data from the server and stores it in the specified DataStorage.
     *
     * @param dataStorage  the DataStorage instance to use for storing data
     * @param serverUri    the URI of the server to connect to
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void readData(DataStorage dataStorage, URI serverUri) throws IOException {
        this.dataStorage = dataStorage;
        connectToServer();
    }

    private void connectToServer() {
        try {
            this.connect();
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
