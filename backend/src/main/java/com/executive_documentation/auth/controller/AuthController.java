package com.executive_documentation.auth.controller;

import com.executive_documentation.auth.config.JwtTokenProvider;
import com.executive_documentation.auth.dto.AuthResponse;
import com.executive_documentation.auth.dto.LoginDto;
import com.executive_documentation.auth.dto.UserInfoDto;
import com.executive_documentation.auth.model.AppUser;
import com.executive_documentation.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        // Здесь можно добавить дополнительную логику (например, получение email, ролей и т.д.)
        return ResponseEntity.ok(new UserInfoDto(username));
    }

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@RequestBody LoginDto loginDto) {
        AppUser user = authService.registerUser(loginDto);
        String jwt = jwtTokenProvider.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody LoginDto loginDto) {
        AppUser user = authService.registerAdmin(loginDto);
        String jwt = jwtTokenProvider.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Можно добавить логику инвалидации токена, если нужно
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
