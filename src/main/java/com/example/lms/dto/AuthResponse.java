package com.example.lms.dto;

import com.example.lms.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private Role role;
    private Long userId;
    private String message;

    public AuthResponse(String token, String username, Role role, Long userId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.message = "Login successful";
    }
}
