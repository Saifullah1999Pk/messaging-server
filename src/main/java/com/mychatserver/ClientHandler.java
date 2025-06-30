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
    private ChatServer chatServer;


    public ClientHandler(Socket clientSocket, ChatServer chatServer) {
        this.clientSocket = clientSocket;
        this.clientAddress = clientSocket.getInetAddress().getHostAddress();
        this.chatServer = chatServer;
    }

    public void run() {
        String userID = null;
        String targetUserID = null;
        System.out.println("ClientHandler thread started for " + clientAddress + ".");

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            System.out.println("IO streams set up for the client " + clientAddress);

            out.println("Welcome to the Chat Server! Please enter your UserID: ");
            userID = in.readLine();
            if (userID == null || userID.trim().isEmpty()) {
                System.err.println("Client " + clientAddress + " did not provide a UserID. Disconnecting.");
                out.println("Error: UserID required. Disconnecting."); // Inform the client
                return; // Exit the run method, causing cleanup in finally block
            }

            System.out.println("User " + userID + " connected from " + clientAddress);
            chatServer.registerClient(userID, out);

            String clientMessage;
            String targetClientMessage;
            while ((clientMessage = in.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("exit")) {
                    System.out.println("User " + userID + " (" + clientAddress + ") requested disconnect");
                    break;
                }
                String[] parts = clientMessage.split(":", 2);
                if (parts.length == 2){
                    targetUserID= parts[0];
                    targetClientMessage = parts[1];

                    System.out.println("Message from " + userID + " to " + targetUserID + ": " + targetClientMessage);
                    chatServer.sendMessage(userID, targetUserID, targetClientMessage, out);

                }
                else {
                    System.out.println("Received malformed message from " + userID + ": " + clientMessage);
                    out.println("Error: Message must be in the format [TargetUserID]:[YourMessage].");
                }

            }

        } catch (IOException e) {
            System.err.println("I/O error with client " + clientAddress + ": " + e.getMessage());
        } finally {
            System.out.println("ClientHandler for " + clientAddress + " is cleaning up resources.");
            if (userID != null) {
                chatServer.deregisterClient(userID);
            }
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