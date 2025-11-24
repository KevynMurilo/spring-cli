package com.example.demo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request payload")
public class LoginRequest {

        @Schema(description = "Username", example = "admin", required = true)
        private String username;

        @Schema(description = "Password", example = "password123", required = true)
        private String password;
}
