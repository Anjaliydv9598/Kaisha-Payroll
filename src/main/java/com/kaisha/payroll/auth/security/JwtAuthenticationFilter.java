
package com.kaisha.payroll.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(
            @Value("${jwt.secret}") String secret
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret is missing or empty"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Skip public endpoints
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path = request.getServletPath();

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        return path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/admin-login")
                || path.equals("/api/auth/staff-login")
                || path.equals("/api/auth/registration-status")
                || path.startsWith("/api/auth/forgot-password/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            unauthorized(response, "JWT token is missing");
            return;
        }

        try {

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if (username == null || username.isBlank()) {
                unauthorized(response, "JWT username is missing");
                return;
            }

            if (role == null || role.isBlank()) {
                unauthorized(response, "JWT role is missing");
                return;
            }

            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                // Normalize role
                String normalizedRole = role
                        .trim()
                        .toUpperCase(Locale.ROOT);

                if (normalizedRole.startsWith("ROLE_")) {
                    normalizedRole =
                            normalizedRole.substring(5);
                }

                String authority = "ROLE_" + normalizedRole;

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(authority)
                                )
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {

            SecurityContextHolder.clearContext();

            unauthorized(
                    response,
                    "Invalid or expired JWT token"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    private void unauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType("application/json");

        response.getWriter().write(
                "{\"message\":\"" + message + "\"}"
        );
    }
}
