package com.dms.modules.dorm.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dms.common.Result;
import com.dms.modules.dorm.dto.DormRoomAddRequestDTO;
import com.dms.modules.dorm.dto.DormRoomUpdateRequestDTO;
import com.dms.modules.dorm.dto.DormRoomVO;
import com.dms.modules.dorm.entity.DormRoom;
import com.dms.modules.dorm.service.DormRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dorm/rooms")
@RequiredArgsConstructor
@SaCheckLogin
public class DormRoomController {

    private final DormRoomService dormRoomService;

    @GetMapping("/page")
    public Result<Page<DormRoomVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String roomNumber
    ) {
        Page<DormRoom> page = new Page<>(current, size);
        Page<DormRoomVO> result = dormRoomService.getRoomPageWithDuty(page, buildingId, roomNumber);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<DormRoom>> list() {
        return Result.success(dormRoomService.list());
    }

    @GetMapping("/available")
    public Result<List<DormRoomVO>> availableRooms() {
        return Result.success(dormRoomService.getAvailableRooms());
    }

    @PostMapping
    public Result<?> save(@RequestBody @Valid DormRoomAddRequestDTO dto) {
        DormRoom entity = new DormRoom();
        entity.setBuildingId(dto.getBuildingId());
        entity.setRoomNumber(dto.getRoomNumber());
        entity.setCapacity(dto.getCapacity());
        entity.setCurrentCount(dto.getCurrentCount() == null ? 0 : dto.getCurrentCount());
        dormRoomService.save(entity);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(
            @PathVariable Long id,
            @RequestBody @Valid DormRoomUpdateRequestDTO dto
    ) {
        DormRoom entity = dormRoomService.getById(id);
        if (entity == null) {
            return Result.fail("房间不存在");
        }
        entity.setBuildingId(dto.getBuildingId());
        entity.setRoomNumber(dto.getRoomNumber());
        entity.setCapacity(dto.getCapacity());
        entity.setCurrentCount(dto.getCurrentCount() == null ? 0 : dto.getCurrentCount());
        dormRoomService.updateById(entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        dormRoomService.removeById(id);
        return Result.success();
    }
}

