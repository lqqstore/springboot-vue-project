package com.dms.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    /**
     * Sa-Token token 值（前端可存储并在后续请求携带 Authorization）
     */
    private String token;

    private Long userId;

    private String role;
}

