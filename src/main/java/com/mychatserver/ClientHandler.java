    package com.mychatserver;
    import java.io.*;
    import java.net.Socket;

    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket clientSocket){
            this.clientSocket = clientSocket;
        }
        public void run() {
            try {
            System.out.println("ClientHandler thread started.");
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            System.out.println("IO streams set up for the client " + clientSocket.getInetAddress().getHostAddress());
        }
            catch (IOException e) {
                System.err.println("I/O error with client " + clientSocket.getInetAddress().getHostAddress() + ": " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("ClientHandler for " + clientSocket.getInetAddress().getHostAddress() + " is cleaning up resources");
                    if (out!= null) {
                        out.close();
                        System.out.println("Closed PrintWriter for client " + clientSocket.getInetAddress().getHostAddress());
                    }
                try {
                    if (in!= null) {
                        in.close();
                        System.out.println("Closed BufferedReader for client " + clientSocket.getInetAddress().getHostAddress());
                    }
                }
                catch (IOException e) {
                    System.err.println("Error closing BufferedReader for client " +clientSocket.getInetAddress().getHostAddress() + ": " + e.getMessage());
                }
                try {
                    if (clientSocket!= null) {
                        clientSocket.close();
                        System.out.println("Closed ClientSocket for client " + clientSocket.getInetAddress().getHostAddress());
                    }
                }
                catch (IOException e) {
                    System.err.println("Error closing ClientSocket for client " +clientSocket.getInetAddress().getHostAddress() + ": " + e.getMessage());
                }
                }
        }
    }


