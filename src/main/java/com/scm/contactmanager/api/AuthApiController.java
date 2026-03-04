package com.scm.contactmanager.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scm.contactmanager.config.JwtProperties;
import com.scm.contactmanager.dtos.LoginRequest;
import com.scm.contactmanager.dtos.LoginResponse;
import com.scm.contactmanager.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public AuthApiController(AuthenticationManager authenticationManager, 
                            JwtUtil jwtUtil,
                            JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Login endpoint - returns JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Get user details
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                        .token("")
                        .username("")
                        .email("")
                        .build());
            }
            UserDetails userDetails = (UserDetails) principal;

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            // Return response
            LoginResponse response = LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(userDetails.getUsername())
                .email(userDetails.getUsername())
                .expiresIn(jwtProperties.getExpiration() / 1000) // Convert to seconds
                .build();

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponse.builder()
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(LoginResponse.builder()
                    .build());
        }
    }
}
