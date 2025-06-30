package com.mychatserver;

import java.io.IOException;
import java.net.Socket;
public class TestClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 8000;

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            System.out.println("TestClient: Connected to server!");
        }
        catch (IOException e) {
            System.err.println("TestClient: Could not connect to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
