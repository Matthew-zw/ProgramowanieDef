package com.example.projekt.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TotpUtilTest {

    private static final String TEST_SECRET = "JBSWY3DPEHPK3PXP"; // "Hello"

    @Test
    void testGenerateSecret() {
        String secret = TotpUtil.generateSecret();
        assertNotNull(secret);
        assertTrue(secret.matches("[A-Z2-7]{32}"));
    }

    @Test
    void testGenerateCodeIsDeterministic() {
        long fixedTimeStep = 59L; // Z RFC 4226
        String expectedCode = "663767";
        String generatedCode = TotpUtil.generateCode(TEST_SECRET, fixedTimeStep);
        assertEquals(expectedCode, generatedCode);
    }

    @Test
    void testVerifyCodeWithValidCode() {
        long currentTimeStep = System.currentTimeMillis() / 30000L;
        String validCode = TotpUtil.generateCode(TEST_SECRET, currentTimeStep);
        assertTrue(TotpUtil.verifyCode(TEST_SECRET, validCode));
    }

    @Test
    void testVerifyCodeWithInvalidCode() {
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, "000000"));
    }

    @Test
    void testGenerateTotpUri() {
        String uri = TotpUtil.generateTotpUri("MyApp", "user@example.com", TEST_SECRET);
        assertTrue(uri.startsWith("otpauth://totp/MyApp:user%40example.com"));
        assertTrue(uri.contains("secret=" + TEST_SECRET));
        assertTrue(uri.contains("issuer=MyApp"));
    }

    @Test
    void testGenerateQrCode() {
        String uri = "test_uri";
        String qrCode = TotpUtil.generateQrCode(uri);
        assertTrue(qrCode.startsWith("data:image/png;base64,"));
    }
}