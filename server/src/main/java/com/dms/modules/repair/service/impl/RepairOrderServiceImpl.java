package com.dms.modules.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.dorm.entity.DormBuilding;
import com.dms.modules.dorm.entity.DormRoom;
import com.dms.modules.dorm.mapper.DormBuildingMapper;
import com.dms.modules.dorm.mapper.DormRoomMapper;
import com.dms.modules.repair.dto.RepairOrderAddRequestDTO;
import com.dms.modules.repair.dto.RepairOrderUpdateRequestDTO;
import com.dms.modules.repair.dto.RepairOrderVO;
import com.dms.modules.repair.entity.RepairOrder;
import com.dms.modules.repair.mapper.RepairOrderMapper;
import com.dms.modules.repair.service.RepairOrderService;
import com.dms.modules.student.entity.Student;
import com.dms.modules.student.entity.StudentDorm;
import com.dms.modules.student.mapper.StudentDormMapper;
import com.dms.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final DormRoomMapper dormRoomMapper;
    private final DormBuildingMapper dormBuildingMapper;
    private final StudentMapper studentMapper;
    private final StudentDormMapper studentDormMapper;

    @Override
    public List<RepairOrderVO> getRepairOrderList() {
        List<RepairOrder> orders = repairOrderMapper.selectList(null);
        return convertToVOList(orders);
    }

    @Override
    public RepairOrderVO getRepairOrderById(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("报修单不存在");
        }
        List<RepairOrderVO> voList = convertToVOList(List.of(order));
        return voList.get(0);
    }

    @Override
    public RepairOrder addRepairOrder(RepairOrderAddRequestDTO dto) {
        DormRoom room = dormRoomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }

        StudentDorm studentDorm = studentDormMapper.selectOne(
                new LambdaQueryWrapper<StudentDorm>().eq(StudentDorm::getRoomId, dto.getRoomId())
                        .last("limit 1")
        );
        Long studentId = studentDorm != null ? studentDorm.getStudentId() : null;

        RepairOrder order = new RepairOrder();
        order.setStudentId(studentId);
        order.setReporterName(dto.getReporterName());
        order.setRoomId(dto.getRoomId());
        order.setDescription(dto.getDescription());
        order.setStatus(0);
        repairOrderMapper.insert(order);
        return order;
    }

    @Override
    public RepairOrder updateRepairOrder(Long id, RepairOrderUpdateRequestDTO dto) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("报修单不存在");
        }
        if (dto.getDescription() != null) {
            order.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
        }
        if (dto.getHandlerName() != null) {
            order.setHandlerName(dto.getHandlerName());
        }
        repairOrderMapper.updateById(order);
        return order;
    }

    @Override
    public void deleteRepairOrder(Long id) {
        repairOrderMapper.deleteById(id);
    }

    private List<RepairOrderVO> convertToVOList(List<RepairOrder> orders) {
        if (orders.isEmpty()) return List.of();

        List<Long> roomIds = orders.stream().map(RepairOrder::getRoomId).distinct().toList();
        Map<Long, DormRoom> roomMap = dormRoomMapper.selectBatchIds(roomIds)
                .stream().collect(Collectors.toMap(DormRoom::getId, r -> r));

        List<Long> buildingIds = roomMap.values().stream()
                .map(DormRoom::getBuildingId).distinct().toList();
        Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Map.of()
                : dormBuildingMapper.selectBatchIds(buildingIds)
                .stream().collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName));

        List<Long> studentIds = orders.stream()
                .map(RepairOrder::getStudentId).filter(id -> id != null).distinct().toList();
        Map<Long, String> studentNameMap = studentIds.isEmpty() ? Map.of()
                : studentMapper.selectBatchIds(studentIds)
                .stream().collect(Collectors.toMap(Student::getId, Student::getName));

        return orders.stream().map(order -> {
            RepairOrderVO vo = new RepairOrderVO();
            BeanUtils.copyProperties(order, vo);
            DormRoom room = roomMap.get(order.getRoomId());
            if (room != null) {
                vo.setRoomNumber(room.getRoomNumber());
                vo.setBuildingName(buildingNameMap.getOrDefault(room.getBuildingId(), ""));
            }
            if (order.getStudentId() != null) {
                vo.setStudentName(studentNameMap.get(order.getStudentId()));
            }
            return vo;
        }).toList();
    }
}
