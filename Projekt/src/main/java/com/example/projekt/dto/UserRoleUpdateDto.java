package com.example.projekt.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserRoleUpdateDto {
    private Long userId;
    private String username; // Do wyświetlenia
    private Set<Long> roleIds; // Przesyłane będą ID ról, które użytkownik ma mieć
}