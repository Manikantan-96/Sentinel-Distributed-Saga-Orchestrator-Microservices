package com.paymentservice.Security;

import com.paymentservice.DTO.Response.UserResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
        // Prefer storing this as a Base64 string in properties, or ensure length >= 32 bytes for HS256
        @Value("${jwt.secret}")
        private String jwtSecret;

        private SecretKey getSigningKey() {
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        }

        public String generateToken(UserResponseDto userDetails) {
            Date now = new Date();
            Date exp = new Date(now.getTime() + 5 * 60 * 1000); // 5 minutes
            return Jwts.builder()
                    .subject(userDetails.getEmail())
                    .claim("userId",userDetails.getUserId())
                    .issuedAt(now)
                    .expiration(exp)
                    .signWith(getSigningKey())
                    .compact();
        }

        public Claims parseClaims(String token) {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        public boolean isTokenValid(String token) {
            try {
                Claims claims = parseClaims(token);
                Date exp = claims.getExpiration();
                return exp == null || exp.after(new Date());
            } catch (Exception ex) {
                return false;
            }
        }
    }

