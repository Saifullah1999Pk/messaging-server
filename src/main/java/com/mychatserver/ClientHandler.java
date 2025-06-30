package com.mychatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientAddress;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientAddress = clientSocket.getInetAddress().getHostAddress();
    }

    public void run() {
        System.out.println("ClientHandler thread started for " + clientAddress + ".");

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            System.out.println("IO streams set up for the client " + clientAddress);

            out.println("Welcome to the Chat Server! Type 'exit' to disconnect.");
            System.out.println("Sent welcome message to " + clientAddress);

            String clientMessage;

            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received from " + clientAddress + ": " + clientMessage);
                out.println("Server echoed: " + clientMessage);
            }

            System.out.println(clientAddress + " disconnected.");

        } catch (IOException e) {
            System.err.println("I/O error with client " + clientAddress + ": " + e.getMessage());
        } finally {
            System.out.println("ClientHandler for " + clientAddress + " is cleaning up resources.");
            if (out != null) {
                out.close();
                System.out.println("Closed PrintWriter for client " + clientAddress);
            }
            try {
                if (in != null) {
                    in.close();
                    System.out.println("Closed BufferedReader for client " + clientAddress);
                }
            } catch (IOException e) {
                System.err.println("Error closing BufferedReader for client " + clientAddress + ": " + e.getMessage());
            }
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                    System.out.println("Closed ClientSocket for client " + clientAddress);
                }
            } catch (IOException e) {
                System.err.println("Error closing ClientSocket for client " + clientAddress + ": " + e.getMessage());
            }
        }
    }
}