package com.example.foodie.auth.dto;

import java.time.Instant;

public record GeneratedRefreshToken(String token, String jti, Instant expiresAt) {}
