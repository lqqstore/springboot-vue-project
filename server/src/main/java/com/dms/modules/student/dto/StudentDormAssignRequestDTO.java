package com.dms.modules.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentDormAssignRequestDTO {

    private Long studentId;
    private Long roomId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
}
