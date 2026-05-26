package com.financemanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Email(message = "Username must be a valid email format")
        String username,
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,
        
        @NotBlank(message = "Full name is required")
        String fullName,
        
        String phoneNumber
) {}
