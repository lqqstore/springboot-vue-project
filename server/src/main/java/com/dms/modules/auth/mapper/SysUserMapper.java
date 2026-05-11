package com.dms.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dms.modules.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

