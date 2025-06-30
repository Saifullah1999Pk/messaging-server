package com.mychatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {

    public static void main(String[] args) {
        String serverAddress = "localhost";
        String userInput;
        String serverResponse;
        int serverPort = 8000;

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            System.out.println("TestClient: Connected to server!");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner consoleInput = new Scanner(System.in);
            System.out.println("TestClient: Connected to the server!");

            String welcomeMessage = in.readLine();
            if  (welcomeMessage != null) {
                System.out.println("Server: " + welcomeMessage);
            }

            while (true) {
                System.out.println("You are?: ");
                userInput = consoleInput.nextLine();
                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Exiting");
                    break;
                }
                out.println(userInput);
                serverResponse = in.readLine();

                if (serverResponse != null) {
                    System.out.println("Server: " + serverResponse);
                } else {
                    System.out.println("Server disconnected");
                }
            }
        }
        catch (IOException e) {
            System.err.println("TestClient: Could not connect to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
