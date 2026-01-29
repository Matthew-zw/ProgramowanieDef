package com.example.projekt.network;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessagePacket {
    private int sourceNodeId;
    private int[] data;
}