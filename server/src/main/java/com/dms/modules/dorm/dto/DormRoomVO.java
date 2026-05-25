package com.dms.modules.dorm.dto;

import lombok.Data;

@Data
public class DormRoomVO {
    private Long id;
    private Long buildingId;
    private String roomNumber;
    private Integer capacity;
    private Integer currentCount;
    private String buildingName;
    private Long dutyStudentId;
    private String dutyStudentName;
}
