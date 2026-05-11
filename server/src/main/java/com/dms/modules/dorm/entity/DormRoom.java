package com.dms.modules.dorm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dorm_room")
public class DormRoom {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long buildingId;
    private String roomNumber;
    private Integer capacity;
    private Integer currentCount;
}

