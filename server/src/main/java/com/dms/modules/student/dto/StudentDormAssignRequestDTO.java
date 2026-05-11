package com.dms.modules.student.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentDormAssignRequestDTO {
    
    private Long studentId;
    private Long roomId;
    private LocalDate checkInDate;
}
