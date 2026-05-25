package com.dms.modules.dorm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.dorm.dto.DormRoomVO;
import com.dms.modules.dorm.entity.DormBuilding;
import com.dms.modules.dorm.entity.DormRoom;
import com.dms.modules.dorm.entity.RoomDuty;
import com.dms.modules.dorm.mapper.DormBuildingMapper;
import com.dms.modules.dorm.mapper.DormRoomMapper;
import com.dms.modules.dorm.mapper.RoomDutyMapper;
import com.dms.modules.dorm.service.DormRoomService;
import com.dms.modules.student.entity.Student;
import com.dms.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {

    private final DormBuildingMapper dormBuildingMapper;
    private final RoomDutyMapper roomDutyMapper;
    private final StudentMapper studentMapper;

    @Override
    public Page<DormRoomVO> getRoomPageWithDuty(Page<DormRoom> page, Long buildingId, String roomNumber) {
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            wrapper.eq(DormRoom::getBuildingId, buildingId);
        }
        if (StrUtil.isNotBlank(roomNumber)) {
            wrapper.like(DormRoom::getRoomNumber, roomNumber.trim());
        }
        
        Page<DormRoom> roomPage = this.page(page, wrapper);
        
        // 获取所有楼栋信息
        List<Long> buildingIds = roomPage.getRecords().stream()
                .map(DormRoom::getBuildingId)
                .distinct()
                .toList();
        Map<Long, String> buildingNameMap = dormBuildingMapper.selectBatchIds(buildingIds)
                .stream()
                .collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName));
        
        // 获取今天的值日生
        LocalDate today = LocalDate.now();
        List<Long> roomIds = roomPage.getRecords().stream()
                .map(DormRoom::getId)
                .toList();
        LambdaQueryWrapper<RoomDuty> dutyWrapper = new LambdaQueryWrapper<>();
        dutyWrapper.in(RoomDuty::getRoomId, roomIds)
                .eq(RoomDuty::getDutyDate, today);
        List<RoomDuty> dutyList = roomDutyMapper.selectList(dutyWrapper);
        Map<Long, Long> roomDutyStudentMap = dutyList.stream()
                .collect(Collectors.toMap(RoomDuty::getRoomId, RoomDuty::getStudentId));
        
        // 获取学生姓名
        List<Long> studentIds = dutyList.stream()
                .map(RoomDuty::getStudentId)
                .distinct()
                .toList();
        final Map<Long, String> studentNameMap;
        if (!studentIds.isEmpty()) {
            studentNameMap = studentMapper.selectBatchIds(studentIds)
                    .stream()
                    .collect(Collectors.toMap(Student::getId, Student::getName));
        } else {
            studentNameMap = Map.of();
        }
        
        // 转换为VO
        List<DormRoomVO> voList = roomPage.getRecords().stream()
                .map(room -> {
                    DormRoomVO vo = new DormRoomVO();
                    BeanUtils.copyProperties(room, vo);
                    vo.setBuildingName(buildingNameMap.getOrDefault(room.getBuildingId(), "-"));
                    Long dutyStudentId = roomDutyStudentMap.get(room.getId());
                    vo.setDutyStudentId(dutyStudentId);
                    vo.setDutyStudentName(dutyStudentId != null ? studentNameMap.get(dutyStudentId) : null);
                    return vo;
                })
                .toList();
        
        Page<DormRoomVO> voPage = new Page<>(roomPage.getCurrent(), roomPage.getSize(), roomPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<DormRoomVO> getAvailableRooms() {
        // 查询还有容量的房间
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("current_count < capacity");
        List<DormRoom> rooms = this.list(wrapper);
        
        // 获取楼栋信息
        List<Long> buildingIds = rooms.stream()
                .map(DormRoom::getBuildingId)
                .distinct()
                .toList();
        Map<Long, String> buildingNameMap = dormBuildingMapper.selectBatchIds(buildingIds)
                .stream()
                .collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName));
        
        // 转换为VO
        return rooms.stream()
                .map(room -> {
                    DormRoomVO vo = new DormRoomVO();
                    BeanUtils.copyProperties(room, vo);
                    vo.setBuildingName(buildingNameMap.getOrDefault(room.getBuildingId(), "-"));
                    return vo;
                })
                .toList();
    }
}

