package com.dms.modules.auth.controller;

import com.dms.common.Result;
import com.dms.modules.auth.dto.LoginRequestDTO;
import com.dms.modules.auth.dto.LoginResponseDTO;
import com.dms.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        return Result.success(authService.login(dto));
    }
    
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
    
    @GetMapping("/current-user")
    public Result<String> getCurrentUser() {
        return Result.success(StpUtil.getLoginId().toString());
    }
}

