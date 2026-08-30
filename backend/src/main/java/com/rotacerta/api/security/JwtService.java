package com.rotacerta.api.security;

import com.rotacerta.api.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long accessTokenMinutes;

    public JwtService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.access-token-minutes}") long accessTokenMinutes
    ) {
        this.secret = secret;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("name", user.getDisplayName())
                .claim("uid", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Claims claims = claims(token);
        return userDetails.getUsername().equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    public long accessTokenSeconds() {
        return accessTokenMinutes * 60;
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("ROTACERTA_JWT_SECRET deve ter pelo menos 32 bytes.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
