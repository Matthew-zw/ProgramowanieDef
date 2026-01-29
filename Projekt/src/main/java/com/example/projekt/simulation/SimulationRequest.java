package com.example.projekt.simulation;

import lombok.Data;

@Data
public class SimulationRequest {
    public enum SimulationType {
        MESSAGE_CORRUPTION,
        LEADER_FAILURE,
        RANDOM_NODE_FAILURE
    }
    private Long projectId;
    private int corruptionLevel;
    private int paritySymbols;
    private SimulationType simulationType = SimulationType.MESSAGE_CORRUPTION;
}