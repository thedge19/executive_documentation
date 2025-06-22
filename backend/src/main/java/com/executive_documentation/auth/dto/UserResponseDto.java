package com.executive_documentation.auth.dto;

import com.executive_documentation.auth.model.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private String createdAt; // Теперь в строковом формате

    // Форматтер для преобразования даты
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
}
