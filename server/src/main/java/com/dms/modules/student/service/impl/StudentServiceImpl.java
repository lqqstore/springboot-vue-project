package com.dms.modules.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.dorm.entity.DormRoom;
import com.dms.modules.dorm.mapper.DormRoomMapper;
import com.dms.modules.student.dto.StudentAddRequestDTO;
import com.dms.modules.student.dto.StudentDormAssignRequestDTO;
import com.dms.modules.student.dto.StudentUpdateRequestDTO;
import com.dms.modules.student.entity.Student;
import com.dms.modules.student.entity.StudentDorm;
import com.dms.modules.student.mapper.StudentDormMapper;
import com.dms.modules.student.mapper.StudentMapper;
import com.dms.modules.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    
    private final StudentMapper studentMapper;
    private final StudentDormMapper studentDormMapper;
    private final DormRoomMapper dormRoomMapper;
    
    @Override
    public List<Student> getStudentList() {
        return studentMapper.selectList(null);
    }
    
    @Override
    public Student getStudentById(Long id) {
        return studentMapper.selectById(id);
    }
    
    @Override
    public Student getStudentByUserId(Long userId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId);
        return studentMapper.selectOne(wrapper);
    }
    
    @Override
    public Student addStudent(StudentAddRequestDTO dto) {
        Student student = new Student();
        student.setUserId(dto.getUserId());
        student.setName(dto.getName());
        student.setGender(dto.getGender());
        student.setPhone(dto.getPhone());
        student.setMajor(dto.getMajor());
        studentMapper.insert(student);
        return student;
    }
    
    @Override
    public Student updateStudent(Long id, StudentUpdateRequestDTO dto) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new IllegalArgumentException("Student not found");
        }
        student.setName(dto.getName());
        student.setGender(dto.getGender());
        student.setPhone(dto.getPhone());
        student.setMajor(dto.getMajor());
        studentMapper.updateById(student);
        return student;
    }
    
    @Override
    public void deleteStudent(Long id) {
        // Remove dorm assignment if exists
        LambdaQueryWrapper<StudentDorm> dormWrapper = new LambdaQueryWrapper<>();
        dormWrapper.eq(StudentDorm::getStudentId, id);
        StudentDorm studentDorm = studentDormMapper.selectOne(dormWrapper);
        if (studentDorm != null) {
            // Decrease room current count
            DormRoom room = dormRoomMapper.selectById(studentDorm.getRoomId());
            if (room != null && room.getCurrentCount() > 0) {
                room.setCurrentCount(room.getCurrentCount() - 1);
                dormRoomMapper.updateById(room);
            }
            studentDormMapper.delete(dormWrapper);
        }
        studentMapper.deleteById(id);
    }
    
    @Override
    public StudentDorm assignDorm(StudentDormAssignRequestDTO dto) {
        // Check if student already has a dorm assignment
        LambdaQueryWrapper<StudentDorm> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(StudentDorm::getStudentId, dto.getStudentId());
        StudentDorm existingAssignment = studentDormMapper.selectOne(existingWrapper);
        if (existingAssignment != null) {
            throw new IllegalArgumentException("Student already has a dorm assignment");
        }
        
        // Check if room exists and has capacity
        DormRoom room = dormRoomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }
        if (room.getCurrentCount() >= room.getCapacity()) {
            throw new IllegalArgumentException("Room is full");
        }
        
        // Create new assignment
        StudentDorm studentDorm = new StudentDorm();
        studentDorm.setStudentId(dto.getStudentId());
        studentDorm.setRoomId(dto.getRoomId());
        studentDorm.setCheckInDate(dto.getCheckInDate());
        studentDormMapper.insert(studentDorm);
        
        // Increase room current count
        room.setCurrentCount(room.getCurrentCount() + 1);
        dormRoomMapper.updateById(room);
        
        return studentDorm;
    }
    
    @Override
    public void removeDormAssignment(Long studentId) {
        LambdaQueryWrapper<StudentDorm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentDorm::getStudentId, studentId);
        StudentDorm studentDorm = studentDormMapper.selectOne(wrapper);
        if (studentDorm != null) {
            // Decrease room current count
            DormRoom room = dormRoomMapper.selectById(studentDorm.getRoomId());
            if (room != null && room.getCurrentCount() > 0) {
                room.setCurrentCount(room.getCurrentCount() - 1);
                dormRoomMapper.updateById(room);
            }
            studentDormMapper.delete(wrapper);
        }
    }
    
    @Override
    public List<Student> getStudentsByRoomId(Long roomId) {
        // This would typically be done with a join query
        // For simplicity, we'll first get all student-dorm mappings for the room
        LambdaQueryWrapper<StudentDorm> dormWrapper = new LambdaQueryWrapper<>();
        dormWrapper.eq(StudentDorm::getRoomId, roomId);
        List<StudentDorm> studentDorms = studentDormMapper.selectList(dormWrapper);
        
        // Then get the students
        return studentDorms.stream()
                .map(sd -> studentMapper.selectById(sd.getStudentId()))
                .filter(s -> s != null)
                .toList();
    }
    
    @Override
    public StudentDorm getStudentDormAssignment(Long studentId) {
        LambdaQueryWrapper<StudentDorm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentDorm::getStudentId, studentId);
        return studentDormMapper.selectOne(wrapper);
    }
}
