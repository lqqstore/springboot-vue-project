package com.dms.modules.auth.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dms.modules.auth.dto.LoginRequestDTO;
import com.dms.modules.auth.dto.LoginResponseDTO;
import com.dms.modules.auth.entity.SysUser;
import com.dms.modules.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import cn.dev33.satoken.stp.StpUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .last("limit 1");

        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已禁用");
        }

        // 说明：数据库建议存储 bcrypt 哈希值
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        StpUtil.login(user.getId());

        return new LoginResponseDTO(
                StpUtil.getTokenValue(),
                user.getId(),
                user.getRole()
        );
    }
}

