package com.example.projekt.simulation;

import com.example.projekt.Entity.Project;
import com.example.projekt.network.NetworkGraph;
import com.example.projekt.network.NetworkPacket;
import com.example.projekt.network.ServerNode;
import com.example.projekt.reedsolomon.ReedSolomon;
import com.example.projekt.service.ProjectService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final ProjectService projectService;
    private final MeterRegistry meterRegistry;
    private final Random random = new Random();
    private static final int BASE_PORT = 9000;

    public SimulationResult runSimulation(SimulationRequest request) {

        Project project = projectService.findProjectEntityById(request.getProjectId());


        String originalMessage = (project.getDescription() != null && !project.getDescription().isBlank())
                ? project.getDescription()
                : "Default Project Description";

        SimulationResult result = new SimulationResult();
        result.setProjectId(project.getId());
        result.setProjectName(project.getName());
        result.setParitySymbols(request.getParitySymbols());
        result.setCorruptionLevel(request.getCorruptionLevel());
        result.setOriginalMessage(originalMessage);


        byte[] messageBytesRaw = originalMessage.getBytes(StandardCharsets.UTF_8);
        if (messageBytesRaw.length + request.getParitySymbols() > 255) {
            result.setSetupError("Błąd: Długość wiadomości + parzystość > 255 (ograniczenie GF(256)).");
            return result;
        }


        int[] data = toIntArray(messageBytesRaw);
        ReedSolomon rs = new ReedSolomon();
        int[] encoded = rs.encode(data, request.getParitySymbols());
        result.setEncodedMessageHex(toHexString(encoded));


        NetworkGraph network = new NetworkGraph(4, BASE_PORT);


        meterRegistry.counter("simulation.start", "type", request.getSimulationType().name()).increment();

        try {
            network.startAllNodes();


            boolean allReady = network.awaitReady(5, TimeUnit.SECONDS);
            if (!allReady) {
                result.setSetupError("Krytyczny błąd: Węzły sieci nie uruchomiły się w wyznaczonym czasie.");
                return result;
            }


            try { Thread.sleep(500); } catch (InterruptedException e) {}

            int leaderId = network.getLeader().getId();


            switch (request.getSimulationType()) {
                case LEADER_FAILURE:
                    ServerNode oldLeader = network.getLeader();
                    network.disableNode(oldLeader.getId());
                    result.setSetupError(String.format("SCENARIUSZ: Awaria Lidera. Węzeł %d wyłączony.", oldLeader.getId()));

                    ServerNode newLeader = network.electNewLeader();
                    if (newLeader != null) {
                        leaderId = newLeader.getId();
                        result.setSetupError(result.getSetupError() + String.format("\nNowy lider: Węzeł %d przejmuje nadawanie.", leaderId));
                        newLeader.startBroadcast(encoded);
                    } else {
                        result.setSetupError("Błąd: Brak dostępnych węzłów do przejęcia roli lidera.");
                        return result;
                    }
                    break;

                case RANDOM_NODE_FAILURE:
                    int nodeIdToDisable;
                    do {

                        nodeIdToDisable = random.nextInt(3) + 2;
                    } while (nodeIdToDisable == leaderId);

                    network.disableNode(nodeIdToDisable);
                    result.setSetupError(String.format("SCENARIUSZ: Awaria Węzła. Węzeł %d został wyłączony.", nodeIdToDisable));


                    network.getLeader().startBroadcast(encoded);
                    break;

                case MESSAGE_CORRUPTION:
                default:
                    network.getLeader().startBroadcast(encoded);
                    break;
            }

            waitForResults(network);

            processNodeResults(network, result, request, data.length, leaderId);

        } finally {
            network.stopAllNodes();
        }

        return result;
    }


    private void waitForResults(NetworkGraph network) {
        long startTime = System.currentTimeMillis();
        long maxWaitTime = 8000;

        while (System.currentTimeMillis() - startTime < maxWaitTime) {
            boolean allFinished = true;
            int activeNodes = 0;

            for (ServerNode node : network.getAllNodes()) {
                if (node.getRunning().get()) {
                    activeNodes++;
                    if (node.getLastProcessedPacket() == null) {
                        allFinished = false;
                        break;
                    }
                }
            }

            if (activeNodes > 0 && allFinished) {
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                return;
            }

            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
    }


    private void processNodeResults(NetworkGraph network, SimulationResult result, SimulationRequest request, int originalLen, int currentLeaderId) {
        List<SimulationResult.NodeResult> nodeResults = new ArrayList<>();

        for (ServerNode node : network.getAllNodes()) {
            SimulationResult.NodeResult res = new SimulationResult.NodeResult();
            res.setNodeId(node.getId());
            String nodeIdStr = String.valueOf(node.getId());


            if (!node.getRunning().get()) {
                res.setCorrectionSuccess(false);
                res.setErrorMessage("Węzeł wyłączony (symulacja awarii).");
                res.setLog(node.getLog().toString());
                nodeResults.add(res);


                meterRegistry.counter("node.status", "id", nodeIdStr, "status", "dead").increment();
                continue;
            }

            NetworkPacket packet = node.getLastProcessedPacket();


            if (packet == null) {
                res.setCorrectionSuccess(false);
                res.setErrorMessage("TIMEOUT: Węzeł nie otrzymał danych.");
                res.setLog(node.getLog().toString());
                nodeResults.add(res);


                meterRegistry.counter("node.status", "id", nodeIdStr, "status", "timeout").increment();
                continue;
            }


            meterRegistry.counter("node.status", "id", nodeIdStr, "status", "received").increment();

            int[] receivedData = packet.getPayload().clone();

            if (node.getId() == currentLeaderId) {
                res.setCorruptedMessageHex(toHexString(receivedData));
                res.setCorrectionSuccess(true);
                res.setDecodedMessage(result.getOriginalMessage() + " (Źródło)");
                res.setLog(node.getLog().toString());
                nodeResults.add(res);
                continue;
            }

            if (request.getSimulationType() == SimulationRequest.SimulationType.MESSAGE_CORRUPTION) {
                corruptData(receivedData, request.getCorruptionLevel(), node.getLog());

                meterRegistry.counter("simulation.errors.injected", "id", nodeIdStr).increment(request.getCorruptionLevel());
            }
            res.setCorruptedMessageHex(toHexString(receivedData));

            try {
                ReedSolomon rs = new ReedSolomon();
                int[] decoded = rs.decode(receivedData, result.getParitySymbols());

                byte[] decodedBytes = new byte[originalLen];
                System.arraycopy(toByteArray(decoded), 0, decodedBytes, 0, originalLen);
                String msg = new String(decodedBytes, StandardCharsets.UTF_8);

                res.setDecodedMessage(msg);
                res.setCorrectionSuccess(true);
                node.getLog().append("Reed-Solomon decoding successful.\n");

                meterRegistry.counter("rs.decoding", "id", nodeIdStr, "result", "success").increment();

            } catch (Exception e) {
                res.setCorrectionSuccess(false);
                res.setErrorMessage("Błąd dekodowania RS: " + e.getMessage());
                node.getLog().append("Reed-Solomon decoding failed: " + e.getMessage() + "\n");

                meterRegistry.counter("rs.decoding", "id", nodeIdStr, "result", "failure").increment();
            }

            res.setLog(node.getLog().toString());
            nodeResults.add(res);
        }
        result.setNodeResults(nodeResults);
    }



    private int[] toIntArray(byte[] bytes) {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) ints[i] = bytes[i] & 0xFF;
        return ints;
    }

    private byte[] toByteArray(int[] ints) {
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) bytes[i] = (byte) ints[i];
        return bytes;
    }

    private String toHexString(int[] data) {
        return IntStream.of(data)
                .mapToObj(b -> String.format("%02X", b))
                .collect(Collectors.joining(" "));
    }

    private void corruptData(int[] data, int numErrors, StringBuilder log) {
        if (numErrors <= 0) return;
        log.append(String.format("Wprowadzanie %d losowych błędów...\n", numErrors));

        List<Integer> indices = IntStream.range(0, data.length).boxed().collect(Collectors.toList());
        java.util.Collections.shuffle(indices);

        for (int i = 0; i < numErrors && i < indices.size(); i++) {
            int idx = indices.get(i);
            int oldVal = data[idx];
            int newVal = (oldVal + 1 + random.nextInt(254)) % 256;
            data[idx] = newVal;
            log.append(String.format(" -> Błąd na poz %d: %02X -> %02X\n", idx, oldVal, newVal));
        }
    }
}