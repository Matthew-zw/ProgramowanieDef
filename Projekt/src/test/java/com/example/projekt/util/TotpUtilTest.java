package com.example.projekt.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TotpUtilTest {

    private static final String TEST_SECRET = "JBSWY3DPEHPK3PXP";

    @Test
    void testGenerateSecret() {
        String secret = TotpUtil.generateSecret();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());

        assertEquals(32, secret.length());
        assertTrue(secret.matches("[A-Z2-7]+"));
    }

    @Test
    void testGenerateCode_deterministic() {

        long fixedTimeStep = 55962880L;


        String expectedCode = "243655";

        String generatedCode = TotpUtil.generateCode(TEST_SECRET, fixedTimeStep);
        assertNotNull(generatedCode);
        assertEquals(6, generatedCode.length());
        assertEquals(expectedCode, generatedCode);
    }

    @Test
    void testVerifyCode_validCode() {

        long currentTimeStep = System.currentTimeMillis() / 30000L;
        String validCode = TotpUtil.generateCode(TEST_SECRET, currentTimeStep);

        assertTrue(TotpUtil.verifyCode(TEST_SECRET, validCode));
    }

    @Test
    void testVerifyCode_validCode_withTimeSkew_minus1() {
        long currentTimeStep = System.currentTimeMillis() / 30000L;
        String validCode = TotpUtil.generateCode(TEST_SECRET, currentTimeStep - 1);
        assertTrue(TotpUtil.verifyCode(TEST_SECRET, validCode));
    }

    @Test
    void testVerifyCode_validCode_withTimeSkew_plus1() {
        long currentTimeStep = System.currentTimeMillis() / 30000L;
        String validCode = TotpUtil.generateCode(TEST_SECRET, currentTimeStep + 1);
        assertTrue(TotpUtil.verifyCode(TEST_SECRET, validCode));
    }

    @Test
    void testVerifyCode_invalidCode_outsideTimeSkew() {
        long currentTimeStep = System.currentTimeMillis() / 30000L;
        String invalidCode = TotpUtil.generateCode(TEST_SECRET, currentTimeStep + 2);
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, invalidCode));

        invalidCode = TotpUtil.generateCode(TEST_SECRET, currentTimeStep - 2);
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, invalidCode));
    }

    @Test
    void testVerifyCode_incorrectCode() {
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, "999999"));
    }

    @Test
    void testVerifyCode_nullCode() {
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, null));
    }

    @Test
    void testVerifyCode_emptyCode() {
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, ""));
    }

    @Test
    void testVerifyCode_shortCode() {
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, "123"));
    }

    @Test
    void testVerifyCode_longCode() {
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, "1234567"));
    }

    @Test
    void testVerifyCode_nonNumericCode() {
        assertFalse(TotpUtil.verifyCode(TEST_SECRET, "ABCDEF"));
    }

    @Test
    void testGenerateTotpUri() {
        String issuer = "MyCompany";
        String username = "testuser@example.com";
        String secret = "TESTSECRET123";
        String expectedUri = "otpauth://totp/MyCompany:testuser%40example.com?secret=TESTSECRET123&issuer=MyCompany&algorithm=SHA1&digits=6&period=30";
        String actualUri = TotpUtil.generateTotpUri(issuer, username, secret);
        assertEquals(expectedUri, actualUri);
    }

    @Test
    void testGenerateQrCode() {
        String uri = "otpauth://totp/Test:User?secret=JBSWY3DPEHPK3PXP&issuer=Test&algorithm=SHA1&digits=6&period=30";
        String qrCodeBase64 = TotpUtil.generateQrCode(uri);
        assertNotNull(qrCodeBase64);
        assertFalse(qrCodeBase64.isEmpty());
        assertTrue(qrCodeBase64.startsWith("data:image/png;base64,"));
    }
}