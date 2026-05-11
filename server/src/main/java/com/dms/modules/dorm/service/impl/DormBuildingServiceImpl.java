package com.dms.modules.dorm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.dorm.entity.DormBuilding;
import com.dms.modules.dorm.mapper.DormBuildingMapper;
import com.dms.modules.dorm.service.DormBuildingService;
import org.springframework.stereotype.Service;

@Service
public class DormBuildingServiceImpl extends ServiceImpl<DormBuildingMapper, DormBuilding> implements DormBuildingService {
}

