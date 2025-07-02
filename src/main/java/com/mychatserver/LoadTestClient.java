package com.mychatserver;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTestClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8000;
    private static final int NUM_CLIENTS = 1000; // Configurable
    private static final int MESSAGES_PER_CLIENT = 10; // Configurable
    private static final List<String> MESSAGE_POOL = Arrays.asList("hello", "test", "ping", "how are you?", "bye");
    private static final int CLIENT_STARTUP_DELAY_MS = 0; // No delay for max concurrency
    private static final int SOCKET_READ_TIMEOUT_MS = 200; // 200 ms timeout for all reads

    private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicInteger totalMessagesSent = new AtomicInteger(0);
    private static final AtomicInteger totalMessagesReceived = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService clientPool = Executors.newFixedThreadPool(NUM_CLIENTS);
        CountDownLatch allDone = new CountDownLatch(NUM_CLIENTS);
        long testStart = System.currentTimeMillis();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientNum = i;
            clientPool.submit(() -> {
                String userId = "user" + clientNum;
                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
                    socket.setSoTimeout(SOCKET_READ_TIMEOUT_MS);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    // Read welcome message
                    try { in.readLine(); } catch (SocketTimeoutException ignored) {}
                    // Send userID
                    out.println(userId);

                    // Optionally, read any offline messages
                    String line;
                    try {
                        while ((line = in.readLine()) != null && line.startsWith("Queued Message")) {
                            // Ignore for now
                        }
                    } catch (SocketTimeoutException ignored) {}

                    Random rand = new Random(clientNum);
                    for (int m = 0; m < MESSAGES_PER_CLIENT; m++) {
                        // Pick a random target user (not self)
                        String targetUser;
                        do {
                            targetUser = "user" + rand.nextInt(NUM_CLIENTS);
                        } while (targetUser.equals(userId));
                        String message = MESSAGE_POOL.get(rand.nextInt(MESSAGE_POOL.size()));
                        String payload = targetUser + ":" + message;
                        long sendTime = System.nanoTime();
                        out.println(payload);
                        totalMessagesSent.incrementAndGet();
                        // Wait for server response (if any)
                        try {
                            String response = in.readLine();
                            if (response != null) {
                                long latency = (System.nanoTime() - sendTime) / 1_000_000; // ms
                                latencies.add(latency);
                                totalMessagesReceived.incrementAndGet();
                            }
                        } catch (SocketTimeoutException ste) {
                            // No response received in time, continue
                        } catch (IOException ignored) {}
                    }
                    // Optionally, send exit
                    out.println("exit");
                } catch (IOException e) {
                    System.err.println("Client " + userId + " error: " + e.getMessage());
                } finally {
                    allDone.countDown();
                }
            });
            if (CLIENT_STARTUP_DELAY_MS > 0) Thread.sleep(CLIENT_STARTUP_DELAY_MS);
        }
        allDone.await();
        long testEnd = System.currentTimeMillis();
        clientPool.shutdown();
        printStats(testStart, testEnd);
    }

    private static void printStats(long start, long end) {
        int sent = totalMessagesSent.get();
        int received = totalMessagesReceived.get();
        long durationMs = end - start;
        double throughput = sent / (durationMs / 1000.0);
        long minLatency = latencies.stream().min(Long::compareTo).orElse(-1L);
        long maxLatency = latencies.stream().max(Long::compareTo).orElse(-1L);
        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(-1);
        System.out.println("\n--- Load Test Results ---");
        System.out.println("Clients: " + NUM_CLIENTS);
        System.out.println("Messages per client: " + MESSAGES_PER_CLIENT);
        System.out.println("Total messages sent: " + sent);
        System.out.println("Total messages received: " + received);
        System.out.println("Test duration: " + durationMs + " ms");
        System.out.printf("Throughput: %.2f messages/sec\n", throughput);
        System.out.println("Latency (ms): min=" + minLatency + ", max=" + maxLatency + ", avg=" + String.format("%.2f", avgLatency));
    }
} 