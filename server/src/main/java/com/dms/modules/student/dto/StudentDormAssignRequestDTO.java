package com.dms.modules.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentDormAssignRequestDTO {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
}
