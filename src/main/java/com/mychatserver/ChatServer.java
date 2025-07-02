package com.mychatserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private final int port;
    private volatile boolean isRunning = true;
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private final Map<String, ClientConnectionDetails> connectedClients;
    private final Map<String, ConcurrentLinkedQueue<String>> offlineMessages;

    public ChatServer(int port){
        this.port = port;
        this.connectedClients = new ConcurrentHashMap<>();
        this.offlineMessages = new ConcurrentHashMap<>();
    }

    public void registerClient(String userID, PrintWriter out){
        ClientConnectionDetails clientDetails = new ClientConnectionDetails(userID, out);
        connectedClients.put(userID,clientDetails);
        System.out.println("User " + userID + " registered. Total clients: " + connectedClients.size());
    }

    public void deregisterClient(String userID){
        connectedClients.remove(userID);
        System.out.println("User " + userID + " deregistered. Total clients: " + connectedClients.size());
    }

    public void sendMessage(String senderUserID, String targetUserID, String messageContent, PrintWriter senderOut){
        ClientConnectionDetails targetClientDetails = connectedClients.get(targetUserID);
        if (targetClientDetails != null){
            PrintWriter recipientOut = targetClientDetails.getPrintWriter();
            recipientOut.println(senderUserID + ":" + messageContent);
            System.out.println("Server: Message from " + senderUserID + " forwarded to " + targetUserID);
        }
        else {
            ConcurrentLinkedQueue<String> userQueue = offlineMessages.computeIfAbsent(targetUserID, k -> new ConcurrentLinkedQueue<>());
            userQueue.add(senderUserID + ":" + messageContent);
            System.out.println("Server: User '" + targetUserID + "' is offline. Message from '" + senderUserID + "' queued.");
            senderOut.println("Info: User '" + targetUserID + "' is offline. Message queued for delivery.");
        }
    }

    public void deliverOfflineMessages(String userID, PrintWriter clientOut){
        ConcurrentLinkedQueue<String> userQueue = offlineMessages.remove(userID);
        if (userQueue != null && !userQueue.isEmpty()){
            System.out.println("Server: Delivering queued messages to " + userID);
            int deliverCount = 0;
            String queuedMessage;

            while ((queuedMessage = userQueue.poll()) != null){
                clientOut.println("Queued Message (from server): " + queuedMessage);
                deliverCount++;
            }
            System.out.println("Server: Delivered " + deliverCount + " queued messages to " + userID + ".");
        } else {
            System.out.println("Server: No offline messages for " + userID + ".");
        }
    }
    public static void main(String[] args) {

        int port = 8000;
        ChatServer server = new ChatServer(port);
        try {
            server.start();
        } catch (IOException e){
            System.err.println("Failed to start Chatserver on port " + port + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        clientThreadPool = Executors.newFixedThreadPool(1000);
        serverSocket = new ServerSocket(port);
        System.out.println("Chatserver started on port " + port);
        System.out.println("Waiting for clients.. ");

        while (isRunning) {
            try {
                Socket clientsocket = serverSocket.accept();
                System.out.println("Client connected to server: " + clientsocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientsocket, this);
                clientThreadPool.submit(clientHandler);
            }
            catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connections: " + e.getMessage());
                }
                else {
                    System.out.println("Server socket closed ");
                }
            }
        }
    }
}