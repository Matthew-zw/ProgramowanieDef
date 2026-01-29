package com.example.projekt.simulation;

import lombok.Data;
import java.util.List;

@Data
public class SimulationResult {
    private Long projectId;
    private String projectName;
    private String originalMessage;
    private String encodedMessageHex;
    private int paritySymbols;
    private int corruptionLevel;
    private List<NodeResult> nodeResults;
    private String setupError;

    @Data
    public static class NodeResult {
        private int nodeId;
        private String corruptedMessageHex;
        private boolean correctionSuccess;
        private String decodedMessage;
        private String log;
        private String errorMessage;
    }
}