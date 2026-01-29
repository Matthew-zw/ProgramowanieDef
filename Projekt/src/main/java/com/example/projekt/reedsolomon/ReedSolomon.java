package com.example.projekt.reedsolomon;

public class ReedSolomon {

    private final ReedSolomonEncoderGF encoder;
    private final ReedSolomonDecoderGF decoder;

    public ReedSolomon() {
        GenericGF field = GenericGF.QR_CODE_FIELD_256;
        this.encoder = new ReedSolomonEncoderGF(field);
        this.decoder = new ReedSolomonDecoderGF(field);
    }

    public int[] encode(int[] message, int numParitySymbols) {
        int[] toEncode = new int[message.length + numParitySymbols];
        System.arraycopy(message, 0, toEncode, 0, message.length);
        encoder.encode(toEncode, numParitySymbols);
        return toEncode;
    }

    public int[] decode(int[] received, int numParitySymbols) throws Exception {
        int[] data = received.clone();
        decoder.decode(data, numParitySymbols);
        return data;
    }
}