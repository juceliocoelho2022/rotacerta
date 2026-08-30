package com.rotacerta.api.security;

import com.rotacerta.api.dto.AuthDtos.AuthResponse;
import com.rotacerta.api.dto.AuthDtos.AuthUserResponse;
import com.rotacerta.api.dto.AuthDtos.LoginRequest;
import com.rotacerta.api.model.AppUser;
import com.rotacerta.api.model.RefreshToken;
import com.rotacerta.api.repository.AppUserRepository;
import com.rotacerta.api.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final long refreshTokenDays;

    public AuthService(
            AuthenticationManager authenticationManager,
            AppUserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            @Value("${app.security.refresh-token-days}") long refreshTokenDays
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password())
        );

        AppUser user = findUser(request.email());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido."));

        if (current.isRevoked() || current.isExpired() || !current.getUser().isActive()) {
            throw new BadCredentialsException("Refresh token expirado ou revogado.");
        }

        current.revoke();
        refreshTokenRepository.save(current);
        return issueTokens(current.getUser());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken)).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me(String email) {
        return toUserResponse(findUser(email));
    }

    private AuthResponse issueTokens(AppUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = newRefreshTokenValue();
        RefreshToken refreshToken = new RefreshToken(
                user,
                hash(rawRefreshToken),
                OffsetDateTime.now().plusDays(refreshTokenDays)
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                jwtService.accessTokenSeconds(),
                toUserResponse(user)
        );
    }

    private AppUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado."));
    }

    private AuthUserResponse toUserResponse(AppUser user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getCustomerId(),
                user.getDriverId()
        );
    }

    private String newRefreshTokenValue() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Refresh token é obrigatório.");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }
}
