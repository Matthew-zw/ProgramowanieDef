package com.example.projekt.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class UserRegistrationDto {

    @NotBlank(message = "Nazwa użytkownika jest wymagana.")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków.")
    private String username;

    @NotBlank(message = "Hasło jest wymagane.")
    @Size(min = 6, max = 100, message = "Hasło musi mieć od 6 do 100 znaków.")
    private String password;

    @NotBlank(message = "Potwierdzenie hasła jest wymagane.")
    private String confirmPassword;

    @NotBlank(message = "Imię i nazwisko jest wymagane.")
    @Size(max = 100, message = "Imię i nazwisko nie może przekraczać 100 znaków.")
    private String fullName;

    @NotBlank(message = "Adres email jest wymagany.")
    @Email(message = "Nieprawidłowy format adresu email.")
    @Size(max = 100, message = "Adres email nie może przekraczać 100 znaków.")
    private String email;

    // Walidator na poziomie klasy, aby sprawdzić, czy hasła się zgadzają
    @AssertTrue(message = "Hasła muszą być takie same.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}