package com.dms.modules.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.repair.dto.RepairOrderAddRequestDTO;
import com.dms.modules.repair.dto.RepairOrderUpdateRequestDTO;
import com.dms.modules.repair.entity.RepairOrder;
import com.dms.modules.repair.mapper.RepairOrderMapper;
import com.dms.modules.repair.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {
    
    private final RepairOrderMapper repairOrderMapper;
    
    @Override
    public List<RepairOrder> getRepairOrderList() {
        return repairOrderMapper.selectList(null);
    }
    
    @Override
    public List<RepairOrder> getRepairOrdersByStudentId(Long studentId) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrder::getStudentId, studentId);
        return repairOrderMapper.selectList(wrapper);
    }
    
    @Override
    public RepairOrder getRepairOrderById(Long id) {
        return repairOrderMapper.selectById(id);
    }
    
    @Override
    public RepairOrder addRepairOrder(RepairOrderAddRequestDTO dto) {
        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setStudentId(dto.getStudentId());
        repairOrder.setRoomId(dto.getRoomId());
        repairOrder.setDescription(dto.getDescription());
        repairOrder.setStatus(0); // 0待处理
        repairOrderMapper.insert(repairOrder);
        return repairOrder;
    }
    
    @Override
    public RepairOrder updateRepairOrder(Long id, RepairOrderUpdateRequestDTO dto) {
        RepairOrder repairOrder = repairOrderMapper.selectById(id);
        if (repairOrder == null) {
            throw new IllegalArgumentException("Repair order not found");
        }
        if (dto.getDescription() != null) {
            repairOrder.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            repairOrder.setStatus(dto.getStatus());
        }
        if (dto.getHandlerId() != null) {
            repairOrder.setHandlerId(dto.getHandlerId());
        }
        repairOrderMapper.updateById(repairOrder);
        return repairOrder;
    }
    
    @Override
    public void deleteRepairOrder(Long id) {
        repairOrderMapper.deleteById(id);
    }
}
