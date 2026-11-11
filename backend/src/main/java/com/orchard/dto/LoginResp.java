package com.orchard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResp {
    private String token;
    private Long id;
    private String username;
    private String nickname;
    private String role;
}
