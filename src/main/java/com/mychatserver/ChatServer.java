package com.mychatserver;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final int port;
    private volatile boolean isRunning = true;
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;

    public ChatServer(int port){
        this.port = port;
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
        clientThreadPool = Executors.newFixedThreadPool(10);
        serverSocket = new ServerSocket(port);
        System.out.println("Chatserver started on port " + port);
        System.out.println("Waiting for clients.. ");

        while (isRunning) {
            try {
                Socket clientsocket = serverSocket.accept();
                System.out.println("Client connected to server: " + clientsocket.getInetAddress().getHostAddress());
                clientsocket.close();
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



