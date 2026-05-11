package com.dms.modules.repair.dto;

import lombok.Data;

@Data
public class RepairOrderAddRequestDTO {
    
    private Long studentId;
    private Long roomId;
    private String description;
}
