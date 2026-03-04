package com.scm.contactmanager.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String username;
    private String email;
    private Long expiresIn;

    public LoginResponse(String token, String username, String email, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.expiresIn = expiresIn;
    }
}
