package com.executive_documentation.auth.service;

import com.executive_documentation.auth.dto.LoginDto;
import com.executive_documentation.auth.model.AppUser;
import com.executive_documentation.auth.model.UserRole;
import com.executive_documentation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AppUser registerUser(LoginDto loginDto) {
        if (userRepository.findByUsername(loginDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(loginDto.getUsername());
        appUser.setPassword(passwordEncoder.encode(loginDto.getPassword()));
        appUser.setRole(UserRole.ROLE_USER);

        return userRepository.save(appUser);
    }

    public AppUser registerAdmin(LoginDto loginDto) {
        if (userRepository.findByUsername(loginDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(loginDto.getUsername());
        appUser.setPassword(passwordEncoder.encode(loginDto.getPassword()));
        appUser.setRole(UserRole.ROLE_ADMIN);

        return userRepository.save(appUser);
    }
}
