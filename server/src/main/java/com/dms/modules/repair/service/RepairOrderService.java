package com.dms.modules.repair.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dms.modules.repair.dto.RepairOrderAddRequestDTO;
import com.dms.modules.repair.dto.RepairOrderUpdateRequestDTO;
import com.dms.modules.repair.dto.RepairOrderVO;
import com.dms.modules.repair.entity.RepairOrder;

import java.util.List;

public interface RepairOrderService extends IService<RepairOrder> {
    
    List<RepairOrderVO> getRepairOrderList();
    
    RepairOrderVO getRepairOrderById(Long id);
    
    RepairOrder addRepairOrder(RepairOrderAddRequestDTO dto);
    
    RepairOrder updateRepairOrder(Long id, RepairOrderUpdateRequestDTO dto);
    
    void deleteRepairOrder(Long id);
}
