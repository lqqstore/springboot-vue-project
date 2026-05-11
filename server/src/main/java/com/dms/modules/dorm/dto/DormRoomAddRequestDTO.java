package com.dms.modules.dorm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DormRoomAddRequestDTO {

    @NotNull(message = "buildingId不能为空")
    private Long buildingId;

    @NotBlank(message = "房间号不能为空")
    private String roomNumber;

    @NotNull(message = "capacity不能为空")
    @Min(value = 1, message = "capacity 至少为 1")
    private Integer capacity;

    @Min(value = 0, message = "currentCount 不能为负数")
    private Integer currentCount;
}

