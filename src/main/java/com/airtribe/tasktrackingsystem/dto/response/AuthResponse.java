package com.airtribe.tasktrackingsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username ;
    private String email;
    private String role;
    private Long userId;

    public AuthResponse(String token, String username, String email, String role, Long userId){
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }
}










