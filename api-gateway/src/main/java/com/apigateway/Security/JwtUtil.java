package com.apigateway.Security;

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
    @Value("${jwt.secret}")
    private String Secret;
    public SecretKey getKey(){
        return Keys.hmacShaKeyFor(Secret.getBytes(StandardCharsets.UTF_8));
    }

    public Boolean verifyExpiry(String token) {
        Date exp=validateAndParse(token).getExpiration();
        return exp != null && exp.after(new Date(System.currentTimeMillis()));
    }

    public Claims validateAndParse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
