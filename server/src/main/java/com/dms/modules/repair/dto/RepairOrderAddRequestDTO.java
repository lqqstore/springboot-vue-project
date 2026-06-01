package com.dms.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairOrderAddRequestDTO {

    @NotBlank(message = "请输入报修人")
    private String reporterName;

    @NotNull(message = "请选择房间")
    private Long roomId;

    @NotBlank(message = "请输入报修描述")
    private String description;
}
