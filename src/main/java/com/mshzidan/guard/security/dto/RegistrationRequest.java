package com.mshzidan.guard.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrationRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
}
