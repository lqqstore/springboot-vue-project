package com.dms.modules.student.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("student_dorm")
public class StudentDorm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("room_id")
    private Long roomId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("check_in_date")
    private LocalDate checkInDate;
}

