package com.dms.modules.dorm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dorm_building")
public class DormBuilding {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;
    private String location;
}

