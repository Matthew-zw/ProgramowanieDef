package com.example.projekt.dto;


import lombok.Data;

@Data
public final class TwoFactorSetupDto {
    private boolean enabled;
    private String secret;
    private String qrCodeUri;
    private String manualEntryKey;
}