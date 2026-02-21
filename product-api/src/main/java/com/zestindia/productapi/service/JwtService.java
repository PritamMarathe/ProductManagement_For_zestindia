package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @Value("${jwt.expiration}")
    private String expirationStr;   // temporary

    private final UserService userService;

    public JwtService(@Lazy UserService userService) {
        this.userService = userService;
        System.out.println("Raw expiration value: [" + expirationStr + "]");
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, expiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long exp) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Refresh token rotation: Generate new refresh on use
    public AuthResponse refreshToken(String refreshToken) {
        // Validate old refresh, generate new access and new refresh
        String username = extractUsername(refreshToken);
        if (username != null && !isTokenExpired(refreshToken)) {
            // In prod, blacklist old refresh or use DB for rotation
            UserDetails user = userService.loadUserByUsername(username);
            String newAccess = generateToken(user);
            String newRefresh = generateRefreshToken(user);
            AuthResponse response = new AuthResponse();
            response.setAccessToken(newAccess);
            response.setRefreshToken(newRefresh);
            return response;
        }
        throw new RuntimeException("Invalid refresh token");
    }
}