package com.dms.modules.student.dto;

import lombok.Data;

@Data
public class StudentVO {
    private Long id;
    private Long userId;
    private String name;
    private String gender;
    private String phone;
    private String major;
    private String dormRoom;
}
