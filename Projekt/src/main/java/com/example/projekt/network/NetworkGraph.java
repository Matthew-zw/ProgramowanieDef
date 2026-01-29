package com.example.projekt.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetworkGraph {
    private final Map<Integer, ServerNode> nodes = new HashMap<>();
    private ServerNode leader;
    private final int numNodes;
    private final ExecutorService executorService;
    private final CountDownLatch readyLatch;

    public NetworkGraph(int numNodes, int basePort) {
        this.numNodes = numNodes;
        this.executorService = Executors.newFixedThreadPool(numNodes);
        this.readyLatch = new CountDownLatch(numNodes);

        for (int i = 1; i <= numNodes; i++) {
            nodes.put(i, new ServerNode(i, basePort, readyLatch));
        }
        this.leader = nodes.get(1);
        createConnections();
    }

    private void createConnections() {
        if (nodes.size() == 4) {
            addConnection(1, 2); addConnection(1, 3); addConnection(1, 4);
            addConnection(2, 3); addConnection(3, 4);
        }
    }

    private void addConnection(int node1Id, int node2Id) {
        ServerNode node1 = nodes.get(node1Id);
        ServerNode node2 = nodes.get(node2Id);
        if (node1 != null && node2 != null) {
            node1.addNeighborPort(node2.getPort());
            node2.addNeighborPort(node1.getPort());
        }
    }

    public void startAllNodes() {
        for (ServerNode node : nodes.values()) {
            executorService.submit(node);
            node.startProcessingLoop();
        }
    }

    public void stopAllNodes() {
        for (ServerNode node : nodes.values()) {
            node.stop();
        }
        executorService.shutdownNow();
    }

    public boolean awaitReady(long timeout, TimeUnit unit) {
        try {
            return readyLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public ServerNode getLeader() {
        return leader;
    }

    public ServerNode getNode(int id) {
        return nodes.get(id);
    }

    public List<ServerNode> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    public void disableNode(int id) {
        ServerNode node = nodes.get(id);
        if (node != null) {
            node.stop();
        }
    }

    public ServerNode electNewLeader() {
        for (ServerNode node : nodes.values()) {
            if (node.getRunning().get() && node.getId() != this.leader.getId()) {
                this.leader = node;
                return node;
            }
        }
        return null;
    }
}