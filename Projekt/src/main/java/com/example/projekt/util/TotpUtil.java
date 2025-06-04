package com.example.projekt.util;



import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TotpUtil {

    private static final int TIME_STEP_SECONDS = 30;
    private static final long TIME_STEP_MILLIS = TimeUnit.SECONDS.toMillis(TIME_STEP_SECONDS);
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int CODE_DIGITS = 6;
    private static final int TIME_SKEW = 1;
    private static final int SECRET_BYTE_LENGTH = 20;
    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[SECRET_BYTE_LENGTH];
        random.nextBytes(secretBytes);
        return Base32Encoder.encode(secretBytes);
    }
    public static String generateCode(String secretBase32, long timeStep) {
        byte[] secretBytes = Base32Encoder.decode(secretBase32);
        byte[] timeStepBytes = longToBytes(timeStep);

        byte[] hmac = generateHMAC(secretBytes, timeStepBytes);
        int offset = hmac[hmac.length - 1] & 0x0F;
        int binary =
                ((hmac[offset] & 0x7F) << 24) |
                        ((hmac[offset + 1] & 0xFF) << 16) |
                        ((hmac[offset + 2] & 0xFF) << 8) |
                        (hmac[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, CODE_DIGITS);

        return String.format("%0" + CODE_DIGITS + "d", otp);
    }
    public static boolean verifyCode(String secretBase32, String code) {
        if (code == null || code.length() != CODE_DIGITS) {
            return false;
        }
        try {
            Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return false;
        }

        long currentTimeStep = getCurrentTimeStep();

        for (int i = -TIME_SKEW; i <= TIME_SKEW; i++) {
            String generatedCode = generateCode(secretBase32, currentTimeStep + i);
            if (generatedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }
    public static String generateTotpUri(String issuer, String username, String secret) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                urlEncode(issuer), urlEncode(username), secret, urlEncode(issuer), CODE_DIGITS, TIME_STEP_SECONDS);
    }
    public static String generateQrCode(String uri) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    uri,
                    BarcodeFormat.QR_CODE,
                    200,
                    200,
                    hints
            );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Błąd podczas generowania kodu QR", e);
        }
    }
    private static long getCurrentTimeStep() {
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis / TIME_STEP_MILLIS;
    }
    private static byte[] longToBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(value);
        return buffer.array();
    }
    private static byte[] generateHMAC(byte[] key, byte[] data) {
        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGORITHM);
            hmac.init(keySpec);
            return hmac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
    private static String urlEncode(String input) {
        return input.replace(":", "%3A").replace(" ", "%20").replace("/", "%2F");
    }

    private static boolean isUrlSafe(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')
                || c == '-' || c == '.' || c == '_' || c == '~';
    }
}