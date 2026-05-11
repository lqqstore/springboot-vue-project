package com.dms.modules.auth.service;

import com.dms.modules.auth.dto.LoginRequestDTO;
import com.dms.modules.auth.dto.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO dto);
}

