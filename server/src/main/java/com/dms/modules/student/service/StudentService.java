package com.dms.modules.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dms.modules.student.entity.Student;
import com.dms.modules.student.entity.StudentDorm;
import com.dms.modules.student.dto.StudentAddRequestDTO;
import com.dms.modules.student.dto.StudentUpdateRequestDTO;
import com.dms.modules.student.dto.StudentDormAssignRequestDTO;

import java.util.List;

public interface StudentService extends IService<Student> {
    
    List<Student> getStudentList();
    
    Student getStudentById(Long id);
    
    Student getStudentByUserId(Long userId);
    
    Student addStudent(StudentAddRequestDTO dto);
    
    Student updateStudent(Long id, StudentUpdateRequestDTO dto);
    
    void deleteStudent(Long id);
    
    // Dorm assignment methods
    StudentDorm assignDorm(StudentDormAssignRequestDTO dto);
    
    void removeDormAssignment(Long studentId);
    
    List<Student> getStudentsByRoomId(Long roomId);
    
    StudentDorm getStudentDormAssignment(Long studentId);
}
