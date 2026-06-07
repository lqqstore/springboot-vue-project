package com.dms.modules.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.dms.modules.auth.dto.LoginRequestDTO;
import com.dms.modules.auth.dto.LoginResponseDTO;
import com.dms.modules.auth.entity.SysUser;
import com.dms.modules.auth.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    
    @Mock
    private SysUserMapper sysUserMapper;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testLogin_Success() {
        // Mock Sa-Token 静态方法（单元测试无 Web 上下文）
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn("mock-token-value");

            // 准备测试数据
            String username = "admin";
            String password = "admin123456";
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            SysUser user = new SysUser();
            user.setId(1L);
            user.setUsername(username);
            user.setPassword(hashedPassword);
            user.setRole("ADMIN");
            user.setStatus(1);

            // 模拟 SysUserMapper 的行为
            when(sysUserMapper.selectOne(any())).thenReturn(user);

            // 执行登录操作
            LoginRequestDTO requestDTO = new LoginRequestDTO();
            requestDTO.setUsername(username);
            requestDTO.setPassword(password);

            LoginResponseDTO responseDTO = authService.login(requestDTO);

            // 验证结果
            assertNotNull(responseDTO);
            assertNotNull(responseDTO.getToken());
            assertEquals(user.getId(), responseDTO.getUserId());
            assertEquals(user.getRole(), responseDTO.getRole());
        }
    }
    
    @Test
    void testLogin_UserNotFound() {
        // 模拟 SysUserMapper 返回 null
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        
        // 执行登录操作，应该抛出异常
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername("non_existent_user");
        requestDTO.setPassword("password");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(requestDTO);
        });
        
        assertEquals("账号不存在", exception.getMessage());
    }
    
    @Test
    void testLogin_InvalidPassword() {
        // 准备测试数据
        String username = "admin";
        String password = "admin123456";
        String wrongPassword = "wrong_password";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole("ADMIN");
        user.setStatus(1);
        
        // 模拟 SysUserMapper 的行为
        when(sysUserMapper.selectOne(any())).thenReturn(user);
        
        // 执行登录操作，应该抛出异常
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword(wrongPassword);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(requestDTO);
        });
        
        assertEquals("密码错误", exception.getMessage());
    }
    
    @Test
    void testLogin_UserDisabled() {
        // 准备测试数据
        String username = "admin";
        String password = "admin123456";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole("ADMIN");
        user.setStatus(0); // 账号禁用
        
        // 模拟 SysUserMapper 的行为
        when(sysUserMapper.selectOne(any())).thenReturn(user);
        
        // 执行登录操作，应该抛出异常
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword(password);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(requestDTO);
        });
        
        assertEquals("账号已禁用", exception.getMessage());
    }
}
