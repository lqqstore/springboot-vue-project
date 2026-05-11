package com.dms.modules.dorm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DormBuildingAddRequestDTO {

    @NotBlank(message = "楼栋名称不能为空")
    private String name;

    private String location;
}

