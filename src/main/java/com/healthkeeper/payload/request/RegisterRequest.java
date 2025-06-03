package com.healthkeeper.payload.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

public class RegisterRequest {
    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    private Set<String> role;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    @Size(max = 20)
    private String phone;

    private String avatar;
} 