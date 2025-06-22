package com.executive_documentation.auth.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String email; // Меняем username на email
    private String password;
}