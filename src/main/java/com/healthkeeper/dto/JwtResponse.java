package com.healthkeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private UserDTO user;

    public JwtResponse(String token, String type, UserDTO user) {
        this.token = token;
        this.type = type;
        this.user = user;
    }

    public JwtResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
} 