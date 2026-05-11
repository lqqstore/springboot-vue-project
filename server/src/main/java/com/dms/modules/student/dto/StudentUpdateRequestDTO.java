package com.dms.modules.student.dto;

import lombok.Data;

@Data
public class StudentUpdateRequestDTO {
    
    private String name;
    private String gender;
    private String phone;
    private String major;
}
