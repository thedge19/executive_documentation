package com.executive_documentation.auth.dto;

import com.executive_documentation.auth.model.AppUser;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class UserMapper {

    public static UserResponseDto appUserToResponseDto(AppUser appUser) {
        return UserResponseDto.builder()
                .id(appUser.getId())
                .username(appUser.getUsername())
                .email(appUser.getEmail())
                .role(appUser.getRole())
                .createdAt(formatDateTime(appUser.getCreatedAt()))
                .build();
    }

    public static AppUser userCreateDtoToEntity(UserCreateDto userCreateDto, PasswordEncoder passwordEncoder) {
        return AppUser.builder()
                .username(userCreateDto.getUsername())
                .email(userCreateDto.getEmail())
                .password(passwordEncoder.encode(userCreateDto.getPassword()))
                .role(userCreateDto.getRole())
                .build();
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(UserResponseDto.formatter);
    }
}
