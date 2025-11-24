package com.example.demo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with JWT token")
public class AuthResponse {

        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;

        @Schema(description = "Token type", example = "Bearer")
        private String type;

        @Schema(description = "Username", example = "admin")
        private String username;
}