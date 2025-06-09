package com.example.projekt.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyTotpCodeDto {
    @NotBlank(message = "Kod jest wymagany.")
    @Size(min = 6, max = 6, message = "Kod musi mieć 6 cyfr.")
    private String code;
}