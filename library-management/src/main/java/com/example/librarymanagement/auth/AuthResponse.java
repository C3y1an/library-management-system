package com.example.librarymanagement.auth;

public record AuthResponse(String username, String displayName, String role, String token) {
}
