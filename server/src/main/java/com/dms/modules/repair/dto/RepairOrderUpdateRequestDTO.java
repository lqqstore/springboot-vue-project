package com.dms.modules.repair.dto;

import lombok.Data;

@Data
public class RepairOrderUpdateRequestDTO {

    private String description;
    private Integer status;
    private String handlerName;
}
