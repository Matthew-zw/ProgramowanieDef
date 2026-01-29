package com.example.projekt.network;

import lombok.Getter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class ServerNode implements Runnable {
    private final int id;
    private final int port;
    private final List<Integer> neighborPorts = new ArrayList<>();
    private final StringBuilder log = new StringBuilder();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final BlockingQueue<NetworkPacket> messageQueue = new LinkedBlockingQueue<>();
    private final List<Integer> processedMessages = Collections.synchronizedList(new ArrayList<>());

    private volatile NetworkPacket lastProcessedPacket = null;
    private ServerSocket serverSocket;
    private final CountDownLatch readyLatch;

    // Pula wątków do wysyłania wiadomości w tle
    private final ExecutorService senderExecutor = Executors.newCachedThreadPool();

    public ServerNode(int id, int basePort, CountDownLatch readyLatch) {
        this.id = id;
        this.port = basePort + id;
        this.readyLatch = readyLatch;
    }

    public void addNeighborPort(int port) {
        this.neighborPorts.add(port);
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                log.append(String.format("Node %d: Error closing socket: %s\n", id, e.getMessage()));
            }
            senderExecutor.shutdownNow();
        }
    }

    @Override
    public void run() {
        running.set(true);
        startServer();
    }

    private void startServer() {
        try {
            this.serverSocket = new ServerSocket(port);
            readyLatch.countDown(); // Sygnalizujemy gotowość
            log.append(String.format("Node %d listening on port %d.\n", id, port));

            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(5000);
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (IOException e) {
                    if (running.get()) {
                        log.append(String.format("Node %d accept error: %s\n", id, e.getMessage()));
                    }
                }
            }
        } catch (IOException e) {
            log.append(String.format("Node %d critical start error: %s\n", id, e.getMessage()));
            // Zwalniamy latch w przypadku błędu, żeby nie zablokować aplikacji
            while (readyLatch.getCount() > 0) readyLatch.countDown();
        } finally {
            stop();
        }
    }

    public void startProcessingLoop() {
        Thread processingThread = new Thread(() -> {
            while (running.get()) {
                try {
                    NetworkPacket packet = messageQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (packet != null) {
                        processPacket(packet);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        processingThread.setName("Processor-" + id);
        processingThread.start();
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            NetworkPacket packet = (NetworkPacket) in.readObject();
            messageQueue.put(packet);
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            // Ignorujemy błędy przy zamykaniu
        }
    }

    private void processPacket(NetworkPacket packet) {
        this.lastProcessedPacket = packet;

        int messageHash = java.util.Arrays.hashCode(packet.getPayload());
        boolean alreadyProcessed;

        synchronized (processedMessages) {
            alreadyProcessed = processedMessages.contains(messageHash);
            if (!alreadyProcessed) {
                processedMessages.add(messageHash);
            }
        }

        if (alreadyProcessed) return;

        log.append(String.format("Node %d received new data. Broadcasting...\n", id));
        broadcast(packet);
    }

    public void startBroadcast(int[] data) {
        NetworkPacket packet = new NetworkPacket(data);
        this.lastProcessedPacket = packet;
        processedMessages.add(java.util.Arrays.hashCode(data));
        log.append(String.format("Node %d (Leader) initiating broadcast.\n", id));
        broadcast(packet);
    }

    private void broadcast(NetworkPacket packet) {
        List<Integer> targets = new ArrayList<>(neighborPorts);
        for (int neighborPort : targets) {
            senderExecutor.submit(() -> sendPacketWithRetry(neighborPort, packet));
        }
    }

    private void sendPacketWithRetry(int targetPort, NetworkPacket packet) {
        int maxRetries = 5;
        int delayMs = 300;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            if (!running.get()) return;

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", targetPort), 1000);
                try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    out.writeObject(packet);
                    out.flush();
                    return; // Sukces
                }
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    try { Thread.sleep(delayMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                }
            }
        }
        if (running.get()) {
            log.append(String.format("Node %d: FAILED to send to %d. Last error: %s\n", id, targetPort, lastException != null ? lastException.getMessage() : "Unknown"));
        }
    }
}