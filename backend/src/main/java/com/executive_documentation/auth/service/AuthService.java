package com.executive_documentation.auth.service;

import com.executive_documentation.auth.dto.LoginDto;
import com.executive_documentation.auth.dto.UserCreateDto;
import com.executive_documentation.auth.dto.UserMapper;
import com.executive_documentation.auth.dto.UserResponseDto;
import com.executive_documentation.auth.model.AppUser;
import com.executive_documentation.auth.model.UserRole;
import com.executive_documentation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUser registerUser(LoginDto loginDto) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.findByEmail(loginDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email уже используется!");
        }

        AppUser appUser = new AppUser();
        appUser.setEmail(loginDto.getEmail());  // Устанавливаем email вместо username
        appUser.setPassword(passwordEncoder.encode(loginDto.getPassword()));
        appUser.setRole(UserRole.ROLE_USER);

        return userRepository.save(appUser);
    }

    public AppUser registerAdmin(LoginDto loginDto) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.findByEmail(loginDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email уже используется!");
        }

        AppUser appUser = new AppUser();
        appUser.setEmail(loginDto.getEmail());  // Устанавливаем email вместо username
        appUser.setPassword(passwordEncoder.encode(loginDto.getPassword()));
        appUser.setRole(UserRole.ROLE_ADMIN);

        return userRepository.save(appUser);
    }

    public List<UserResponseDto> getAllUsers() {
        List<AppUser> users = userRepository.findAll();

        return users.stream()
                .map(UserMapper::appUserToResponseDto)
                .toList();
    }

    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        // Проверка на существование пользователя
        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Создание нового пользователя
        AppUser newUser = UserMapper.userCreateDtoToEntity(userCreateDto, passwordEncoder);

        // Сохранение в БД
        AppUser savedUser = userRepository.save(newUser);

        // Возвращаем DTO без пароля
        return UserMapper.appUserToResponseDto(savedUser);
    }

    public void deleteUser(Long userId) {
        // Проверяем существование пользователя
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Можно добавить дополнительные проверки (например, нельзя удалить самого себя)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName().equals(user.getEmail())) {
            throw new RuntimeException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }
}