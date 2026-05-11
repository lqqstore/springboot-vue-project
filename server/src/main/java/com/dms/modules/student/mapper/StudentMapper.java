package com.dms.modules.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dms.modules.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}

