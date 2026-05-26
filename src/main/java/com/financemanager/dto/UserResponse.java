package com.financemanager.dto;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String phoneNumber
) {}
