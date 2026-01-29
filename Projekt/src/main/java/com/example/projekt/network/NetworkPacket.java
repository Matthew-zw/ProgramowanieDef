package com.example.projekt.network;

import java.io.Serializable;
import java.util.Arrays;

public class NetworkPacket implements Serializable {
    private final int[] payload;

    public NetworkPacket(int[] payload) {
        this.payload = payload;
    }

    public int[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "NetworkPacket{" + "payload=" + Arrays.toString(payload) + '}';
    }
}