package com.rotacerta.api.dto;

import com.rotacerta.api.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}

    public record LogoutRequest(
            @NotBlank String refreshToken
    ) {}

    public record AuthUserResponse(
            Long id,
            String email,
            String displayName,
            UserRole role,
            Long customerId,
            Long driverId
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds,
            AuthUserResponse user
    ) {}
}
