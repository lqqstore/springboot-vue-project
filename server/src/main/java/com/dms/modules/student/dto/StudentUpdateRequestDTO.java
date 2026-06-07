package com.dms.modules.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentUpdateRequestDTO {

    @NotBlank(message = "姓名不能为空")
    private String name;
    private String gender;
    private String phone;
    private String major;
}
