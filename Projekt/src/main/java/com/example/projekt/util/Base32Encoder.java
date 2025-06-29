package com.example.projekt.util;

public class Base32Encoder {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int BITS_PER_CHAR = 5;

    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        int i = 0, index = 0, digit;
        int currentByte, nextByte;
        StringBuilder base32 = new StringBuilder((data.length + 4) * 8 / 5);

        while (i < data.length) {
            currentByte = (data[i] >= 0) ? data[i] : (data[i] + 256);

            if (index > 3) {
                if ((i + 1) < data.length) {
                    nextByte = (data[i + 1] >= 0) ? data[i + 1] : (data[i + 1] + 256);
                } else {
                    nextByte = 0;
                }
                digit = currentByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (currentByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0)
                    i++;
            }
            base32.append(ALPHABET.charAt(digit));
        }


        return base32.toString();
    }


    public static byte[] decode(String data) {
        if (data == null || data.isEmpty()) {
            return new byte[0];
        }

        StringBuilder cleanDataBuilder = new StringBuilder();
        for (char c : data.toUpperCase().toCharArray()) {
            if (ALPHABET.indexOf(c) != -1) {
                cleanDataBuilder.append(c);
            }
        }
        String cleanData = cleanDataBuilder.toString();


        int outputLength = (cleanData.length() * BITS_PER_CHAR) / 8;
        byte[] result = new byte[outputLength];

        int buffer = 0;
        int bitsLeft = 0;
        int byteIndex = 0;

        for (char c : cleanData.toCharArray()) {
            int value = ALPHABET.indexOf(c);

            buffer = (buffer << BITS_PER_CHAR) | value;
            bitsLeft += BITS_PER_CHAR;

            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                result[byteIndex++] = (byte) ((buffer >> bitsLeft) & 0xff);
            }
        }

        return result;
    }
}