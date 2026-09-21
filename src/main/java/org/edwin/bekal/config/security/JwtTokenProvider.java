package org.edwin.bekal.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.jwt-ttl-minutes}")
    private Duration jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Central Parsing Method: Menjamin aturan parsing & clock skew konsisten
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .clockSkewSeconds(60) // Toleransi clock skew 60 detik
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 1. GENERATE TOKEN KARYAWAN (INTERNAL USER)
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration.toMillis());

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .orElse("");

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .claim("user_type", "INTERNAL")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 2. GENERATE TOKEN CUSTOMER
    public String generateTokenForCustomer(Customer customer) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration.toMillis());

        return Jwts.builder()
                .subject(customer.getCustomerEmail())
                .claim("role", "CUSTOMER")
                .claim("user_type", "CUSTOMER")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 3. EXTRACT USERNAME (EMAIL) DARI TOKEN
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // 4. EXTRACT USER_TYPE DARI TOKEN
    public String getUserTypeFromToken(String token) {
        return parseClaims(token).get("user_type", String.class);
    }

    // 5. VALIDASI TOKEN
    public boolean validateToken(String authToken) {
        try {
            parseClaims(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}