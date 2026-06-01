package com.dms.modules.repair.dto;

import lombok.Data;

@Data
public class RepairOrderVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String reporterName;
    private Long roomId;
    private String buildingName;
    private String roomNumber;
    private String description;
    private Integer status;
    private String handlerName;
}
