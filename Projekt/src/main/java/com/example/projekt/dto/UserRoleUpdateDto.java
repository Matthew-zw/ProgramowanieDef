package com.example.projekt.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserRoleUpdateDto {
    private Long userId;
    private String username;
    private Set<Long> roleIds;
}