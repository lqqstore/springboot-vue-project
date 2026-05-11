package com.dms.modules.dorm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.dorm.entity.DormRoom;
import com.dms.modules.dorm.mapper.DormRoomMapper;
import com.dms.modules.dorm.service.DormRoomService;
import org.springframework.stereotype.Service;

@Service
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {
}

