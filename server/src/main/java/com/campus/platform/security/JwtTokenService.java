package com.campus.platform.security;

import com.campus.platform.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;
    private final Integer expireDays;

    public JwtTokenService(AppProperties appProperties) {
        this.secretKey = Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.expireDays = appProperties.getJwt().getExpireDays();
    }

    public String generateToken(JwtUserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(principal.getUserId()))
            .claim("phone", principal.getPhone())
            .claim("role", principal.getRole())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expireDays, ChronoUnit.DAYS)))
            .signWith(secretKey)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

