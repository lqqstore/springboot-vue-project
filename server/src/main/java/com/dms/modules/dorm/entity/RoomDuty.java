package com.dms.modules.dorm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("room_duty")
public class RoomDuty implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("student_id")
    private Long studentId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("duty_date")
    private LocalDate dutyDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("created_at")
    private LocalDateTime createdAt;
}
