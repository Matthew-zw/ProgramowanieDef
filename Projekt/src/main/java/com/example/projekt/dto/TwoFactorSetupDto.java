package com.example.projekt.dto;


import lombok.Data;

@Data
public class TwoFactorSetupDto {
    private boolean enabled;
    private String secret; // Tylko do przekazania nowo wygenerowanego sekretu
    private String qrCodeUri; // Data URI dla obrazka QR
    private String manualEntryKey; // Sekret Base32 do ręcznego wpisania
}