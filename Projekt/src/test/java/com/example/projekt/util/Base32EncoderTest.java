package com.example.projekt.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base32EncoderTest {

    @Test
    void testEncodeEmptyData() {
        byte[] data = new byte[0];
        String encoded = Base32Encoder.encode(data);
        assertTrue(encoded.isEmpty());
    }

    @Test
    void testEncodeNullData() {
        String encoded = Base32Encoder.encode(null);
        assertTrue(encoded.isEmpty());
    }

    @Test
    void testEncodeSimpleString() {
        String input = "Hello";
        byte[] data = input.getBytes();
        String expected = "JBSWY3DP";
        String actual = Base32Encoder.encode(data);
        assertEquals(expected, actual);
    }

    @Test
    void testEncodeHelloWorld() {
        String input = "Hello World";
        byte[] data = input.getBytes();
        String expected = "JBSWY3DPEBLW64TMMQ";
        String actual = Base32Encoder.encode(data);
        assertEquals(expected, actual);
    }


    @Test
    void testDecodeEmptyString() {
        String data = "";
        byte[] decoded = Base32Encoder.decode(data);
        assertEquals(0, decoded.length);
    }

    @Test
    void testDecodeNullString() {
        byte[] decoded = Base32Encoder.decode(null);
        assertEquals(0, decoded.length);
    }

    @Test
    void testDecodeSimpleString() {
        String encoded = "JBSWY3DP";
        byte[] decoded = Base32Encoder.decode(encoded);
        String expected = "Hello";
        assertEquals(expected, new String(decoded));
    }

    @Test
    void testDecodeHelloWorld() {
        String encoded = "JBSWY3DPEBLW64TMMQ";
        byte[] decoded = Base32Encoder.decode(encoded);
        String expected = "Hello World";
        assertEquals(expected, new String(decoded));
    }


    @Test
    void testEncodeDecodeRoundTrip() {
        String original = "The quick brown fox jumps over the lazy dog.";
        byte[] originalBytes = original.getBytes();
        String encoded = Base32Encoder.encode(originalBytes);
        byte[] decodedBytes = Base32Encoder.decode(encoded);
        assertEquals(original, new String(decodedBytes));
    }

    @Test
    void testDecodeHandlesInvalidCharactersAndPadding() {
        String encodedWithInvalidChar = "JBSWY3DP-!@#$%_EB=LW=64=TMMQ";
        byte[] decoded = Base32Encoder.decode(encodedWithInvalidChar);
        String expected = "Hello World";
        assertEquals(expected, new String(decoded));
    }

    @Test
    void testDecodeHandlesLowercase() {
        String encoded = "jbswy3dp";
        byte[] decoded = Base32Encoder.decode(encoded);
        String expected = "Hello";
        assertEquals(expected, new String(decoded));
    }
}