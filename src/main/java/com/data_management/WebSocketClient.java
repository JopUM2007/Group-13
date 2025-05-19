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
     * Constructs a new WebSocketClient with specific server URI and DataStorage.
     *
     * @param serverUri    the URI of the server to connect to
     * @param dataStorage  the DataStorage instance to use to store the data
     */
    public WebSocketClient(URI serverUri, DataStorage dataStorage) {
        super(serverUri);
        this.dataStorage = dataStorage;
    }

    /**
     * Called when the WebSocket connection is opened.
     *
     * @param handShakeData  the server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handShakeData) {
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
     * where patientId is an int, timestamp is long, label is a String,
     * and data is a double.
     *
     * @param message the message string received from the WebSocket server
     * @throws IllegalArgumentException if the message format is invalid or can't be parsed
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
            throw new IllegalArgumentException("Invalid format in message: " + message, e);
        }
    }

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code     the closure code
     * @param reason   the reason for closing
     * @param remote   whether the closure was initiated by the remote host or not
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with exit code " + code + " additional info: " + reason);
    }

    /**
     * Called when an error occurs.
     *
     * @param ex  the exception to the error
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred: " + ex.getMessage());
        onClose(1001, "Error: " + ex.getMessage(), false); // Trigger onClose on error
    }

    /**
     * Reads data from the server and stores it in the specified DataStorage.
     *
     * @param dataStorage  the instance to use for storing data
     * @param serverUri    the URI of the server to connect to
     * @throws IOException if I/O error occurs
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
