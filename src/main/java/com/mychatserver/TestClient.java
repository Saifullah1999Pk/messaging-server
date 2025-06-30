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
        String userInput1, userInput2;
        String serverResponse;
        int serverPort = 8000;

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            System.out.println("TestClient: Connected to server!");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner consoleInput = new Scanner(System.in);
            System.out.println("TestClient: Connected to the server!");

            String welcomeMessage = in.readLine();
            if (welcomeMessage != null) {
                System.out.println("Server: " + welcomeMessage);
            }

            userInput1 = consoleInput.nextLine();
            out.println(userInput1);

            while (true) {
                System.out.println("Enter the target Users ID (or 'exit' to quit) : ");
                userInput1 = consoleInput.nextLine();
                if ("exit".equalsIgnoreCase(userInput1)) {
                    System.out.println("Exiting");
                    break;
                }
                else {
                    System.out.println("Enter message: ");
                    userInput2 = consoleInput.nextLine();
                    out.println(userInput1 + ":" + userInput2);
                    serverResponse = in.readLine();
                    if (serverResponse != null) {
                        System.out.println("Server: " + serverResponse);
                    } else {
                        System.out.println("Server disconnected");
                    }
                }



            }
        } catch (IOException e) {
            System.err.println("TestClient: Could not connect to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
