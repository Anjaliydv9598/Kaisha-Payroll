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

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

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
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path = request.getServletPath();

        return path.startsWith("/api/auth/");
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        System.out.println(
                "JWT FILTER -> "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
        );

        // ==================================================
        // NO AUTHORIZATION HEADER
        // ==================================================

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT FILTER -> Authorization header missing"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        // ==================================================
        // EXTRACT TOKEN
        // ==================================================

        String token =
                authorizationHeader
                        .substring(7)
                        .trim();

        if (token.isEmpty()) {

            System.out.println(
                    "JWT FILTER -> Token is empty"
            );

            unauthorized(
                    response,
                    "JWT token is missing"
            );

            return;
        }

        // ==================================================
        // JWT VALIDATION
        // IMPORTANT:
        // Only JWT parsing is inside try/catch.
        // ==================================================

        try {

            Claims claims =
                    Jwts.parser()
                            .verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

            String username =
                    claims.getSubject();

            String role =
                    claims.get(
                            "role",
                            String.class
                    );

            System.out.println(
                    "JWT FILTER -> Token valid"
            );

            System.out.println(
                    "JWT FILTER -> Username: "
                            + username
            );

            System.out.println(
                    "JWT FILTER -> Role: "
                            + role
            );

            // ==================================================
            // VALIDATE USERNAME
            // ==================================================

            if (username == null ||
                    username.isBlank()) {

                System.out.println(
                        "JWT FILTER -> Username missing"
                );

                unauthorized(
                        response,
                        "JWT username is missing"
                );

                return;
            }

            // ==================================================
            // VALIDATE ROLE
            // ==================================================

            if (role == null ||
                    role.isBlank()) {

                System.out.println(
                        "JWT FILTER -> Role missing"
                );

                unauthorized(
                        response,
                        "JWT role is missing"
                );

                return;
            }

            // ==================================================
            // SET SPRING SECURITY AUTHENTICATION
            // ==================================================

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                String authority;

                if (role.startsWith("ROLE_")) {

                    authority = role;

                } else {

                    authority =
                            "ROLE_" + role;
                }

                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                authority
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );

                System.out.println(
                        "JWT FILTER -> Authentication set"
                );
            }

        } catch (Exception e) {

            // ==================================================
            // JWT VALIDATION FAILED
            // ==================================================

            SecurityContextHolder.clearContext();

            System.out.println(
                    "JWT FILTER -> TOKEN VALIDATION FAILED"
            );

            System.out.println(
                    "JWT FILTER -> "
                            + e.getClass().getSimpleName()
            );

            System.out.println(
                    "JWT FILTER -> "
                            + e.getMessage()
            );

            unauthorized(
                    response,
                    "Invalid or expired JWT token"
            );

            return;
        }

        // ==================================================
        // CONTINUE REQUEST
        //
        // IMPORTANT:
        // This is OUTSIDE the JWT try/catch.
        //
        // If controller/service/database throws an exception,
        // it will NOT be incorrectly reported as a JWT error.
        // ==================================================

        filterChain.doFilter(
                request,
                response
        );
    }

    // ==================================================
    // UNAUTHORIZED RESPONSE
    // ==================================================

    private void unauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write(
                "{\"message\":\""
                        + message
                        + "\"}"
        );
    }
}