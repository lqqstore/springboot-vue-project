package com.dms.modules.student.controller;

import com.dms.common.Result;
import com.dms.modules.student.dto.StudentAddRequestDTO;
import com.dms.modules.student.dto.StudentDormAssignRequestDTO;
import com.dms.modules.student.dto.StudentUpdateRequestDTO;
import com.dms.modules.student.dto.StudentVO;
import com.dms.modules.student.entity.Student;
import com.dms.modules.student.entity.StudentDorm;
import com.dms.modules.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    @GetMapping("/list")
    public Result<List<StudentVO>> getStudentList() {
        List<StudentVO> students = studentService.getStudentList();
        return Result.success(students);
    }
    
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return Result.success(student);
    }
    
    @GetMapping("/user/{userId}")
    public Result<Student> getStudentByUserId(@PathVariable Long userId) {
        Student student = studentService.getStudentByUserId(userId);
        return Result.success(student);
    }
    
    @PostMapping
    public Result<Student> addStudent(@RequestBody @Valid StudentAddRequestDTO dto) {
        Student student = studentService.addStudent(dto);
        return Result.success(student);
    }
    
    @PutMapping("/{id}")
    public Result<Student> updateStudent(@PathVariable Long id, @RequestBody @Valid StudentUpdateRequestDTO dto) {
        Student student = studentService.updateStudent(id, dto);
        return Result.success(student);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success();
    }
    
    // Dorm assignment endpoints
    @PostMapping("/assign-dorm")
    public Result<StudentDorm> assignDorm(@RequestBody @Valid StudentDormAssignRequestDTO dto) {
        StudentDorm assignment = studentService.assignDorm(dto);
        return Result.success(assignment);
    }
    
    @DeleteMapping("/remove-dorm/{studentId}")
    public Result<Void> removeDormAssignment(@PathVariable Long studentId) {
        studentService.removeDormAssignment(studentId);
        return Result.success();
    }
    
    @GetMapping("/room/{roomId}")
    public Result<List<Student>> getStudentsByRoomId(@PathVariable Long roomId) {
        List<Student> students = studentService.getStudentsByRoomId(roomId);
        return Result.success(students);
    }
    
    @GetMapping("/dorm-assignment/{studentId}")
    public Result<StudentDorm> getStudentDormAssignment(@PathVariable Long studentId) {
        StudentDorm assignment = studentService.getStudentDormAssignment(studentId);
        return Result.success(assignment);
    }
}
