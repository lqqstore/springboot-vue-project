package com.dms.modules.student.dto;

import lombok.Data;

@Data
public class StudentAddRequestDTO {
    
    private Long userId;
    private String name;
    private String gender;
    private String phone;
    private String major;
}
