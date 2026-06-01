package com.dms.modules.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.auth.entity.SysUser;
import com.dms.modules.auth.mapper.SysUserMapper;
import com.dms.modules.dorm.entity.DormBuilding;
import com.dms.modules.dorm.entity.DormRoom;
import com.dms.modules.dorm.mapper.DormBuildingMapper;
import com.dms.modules.dorm.mapper.DormRoomMapper;
import com.dms.modules.student.dto.StudentAddRequestDTO;
import com.dms.modules.student.dto.StudentDormAssignRequestDTO;
import com.dms.modules.student.dto.StudentUpdateRequestDTO;
import com.dms.modules.student.dto.StudentVO;
import com.dms.modules.student.entity.Student;
import com.dms.modules.student.entity.StudentDorm;
import com.dms.modules.student.mapper.StudentDormMapper;
import com.dms.modules.student.mapper.StudentMapper;
import com.dms.modules.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    
    private final StudentMapper studentMapper;
    private final StudentDormMapper studentDormMapper;
    private final DormRoomMapper dormRoomMapper;
    private final DormBuildingMapper dormBuildingMapper;
    private final SysUserMapper sysUserMapper;
    
    @Override
    public List<StudentVO> getStudentList() {
        List<Student> students = studentMapper.selectList(null);

        List<StudentDorm> allDorms = studentDormMapper.selectList(null);
        Map<Long, Long> studentRoomMap = allDorms.stream()
                .collect(Collectors.toMap(StudentDorm::getStudentId, StudentDorm::getRoomId));

        List<Long> roomIds = allDorms.stream()
                .map(StudentDorm::getRoomId)
                .distinct()
                .toList();
        Map<Long, DormRoom> roomMap = Map.of();
        if (!roomIds.isEmpty()) {
            roomMap = dormRoomMapper.selectBatchIds(roomIds)
                    .stream()
                    .collect(Collectors.toMap(DormRoom::getId, r -> r));
        }

        final Map<Long, DormRoom> finalRoomMap = roomMap;

        List<Long> buildingIds = finalRoomMap.values().stream()
                .map(DormRoom::getBuildingId)
                .distinct()
                .toList();
        Map<Long, String> buildingNameMap = Map.of();
        if (!buildingIds.isEmpty()) {
            buildingNameMap = dormBuildingMapper.selectBatchIds(buildingIds)
                    .stream()
                    .collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName));
        }

        final Map<Long, String> finalBuildingNameMap = buildingNameMap;

        return students.stream().map(student -> {
            StudentVO vo = new StudentVO();
            vo.setId(student.getId());
            vo.setUserId(student.getUserId());
            vo.setName(student.getName());
            vo.setGender(student.getGender());
            vo.setPhone(student.getPhone());
            vo.setMajor(student.getMajor());

            Long roomId = studentRoomMap.get(student.getId());
            if (roomId != null) {
                DormRoom room = finalRoomMap.get(roomId);
                if (room != null) {
                    String buildingName = finalBuildingNameMap.getOrDefault(room.getBuildingId(), "");
                    vo.setDormRoom(buildingName + " - " + room.getRoomNumber());
                } else {
                    vo.setDormRoom("房间ID:" + roomId);
                }
            }
            return vo;
        }).toList();
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
        SysUser sysUser = new SysUser();
        sysUser.setUsername("stu_" + System.currentTimeMillis());
        sysUser.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw("123456"));
        sysUser.setRole("STUDENT");
        sysUser.setStatus(1);
        sysUserMapper.insert(sysUser);

        Student student = new Student();
        student.setUserId(sysUser.getId());
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
        Student student = studentMapper.selectById(dto.getStudentId());
        if (student == null) {
            throw new IllegalArgumentException("学生不存在");
        }

        DormRoom newRoom = dormRoomMapper.selectById(dto.getRoomId());
        if (newRoom == null) {
            throw new IllegalArgumentException("房间不存在");
        }

        LambdaQueryWrapper<StudentDorm> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(StudentDorm::getStudentId, dto.getStudentId());
        StudentDorm existingAssignment = studentDormMapper.selectOne(existingWrapper);

        if (existingAssignment != null) {
            if (existingAssignment.getRoomId().equals(dto.getRoomId())) {
                throw new IllegalArgumentException("该学生已在此房间，无需重复分配");
            }
            DormRoom oldRoom = dormRoomMapper.selectById(existingAssignment.getRoomId());
            if (oldRoom != null && oldRoom.getCurrentCount() > 0) {
                oldRoom.setCurrentCount(oldRoom.getCurrentCount() - 1);
                dormRoomMapper.updateById(oldRoom);
            }
            studentDormMapper.delete(existingWrapper);
        }

        long currentCount = studentDormMapper.selectCount(
                new LambdaQueryWrapper<StudentDorm>().eq(StudentDorm::getRoomId, dto.getRoomId())
        );
        if (currentCount >= newRoom.getCapacity()) {
            throw new IllegalArgumentException("房间已满");
        }

        StudentDorm studentDorm = new StudentDorm();
        studentDorm.setStudentId(dto.getStudentId());
        studentDorm.setRoomId(dto.getRoomId());
        studentDorm.setCheckInDate(dto.getCheckInDate());
        studentDormMapper.insert(studentDorm);

        newRoom.setCurrentCount((int) currentCount + 1);
        dormRoomMapper.updateById(newRoom);

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
