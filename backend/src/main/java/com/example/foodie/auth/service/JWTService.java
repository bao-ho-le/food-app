package com.example.foodie.auth.service;

import com.example.foodie.auth.config.JwtProperties;
import com.example.foodie.auth.dto.GeneratedRefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Integer userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("jti", UUID.randomUUID().toString())
                .claim("type", "ACCESS")
                .issuer("foodie")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getAccessToken().getExpiration()))
                .signWith(getAccessKey())
                .compact();
    }

    public GeneratedRefreshToken generateRefreshToken(Integer userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshToken().getExpiration());
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("jti", jti)
                .claim("type", "REFRESH")
                .issuer("foodie")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getRefreshKey())
                .compact();

        return new GeneratedRefreshToken(token, jti, expiration.toInstant());
    }

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessToken().getSecret()));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshToken().getSecret()));
    }

    public Claims parseAccessToken(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(getAccessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"ACCESS".equals(claims.get("type", String.class))) {
            throw new JwtException("Not an access token");
        }
        return claims;
    }

    public Claims parseRefreshToken(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(getRefreshKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new JwtException("Not a refresh token");
        }
        return claims;
    }

    public Integer extractUserId(Claims claims) {
        return Integer.parseInt(claims.getSubject());
    }


//    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimResolver.apply(claims);
//    }
//
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser()
//                .verifyWith(getAccessKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }

}
