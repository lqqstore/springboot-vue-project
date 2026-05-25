package com.dms.modules.dorm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dms.modules.dorm.dto.DormRoomVO;
import com.dms.modules.dorm.entity.DormRoom;

import java.util.List;

public interface DormRoomService extends IService<DormRoom> {
    
    Page<DormRoomVO> getRoomPageWithDuty(Page<DormRoom> page, Long buildingId, String roomNumber);
    
    List<DormRoomVO> getAvailableRooms();
}

