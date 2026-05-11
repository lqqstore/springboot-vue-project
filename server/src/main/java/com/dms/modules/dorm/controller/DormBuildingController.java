package com.dms.modules.dorm.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dms.common.Result;
import com.dms.modules.dorm.dto.DormBuildingAddRequestDTO;
import com.dms.modules.dorm.dto.DormBuildingUpdateRequestDTO;
import com.dms.modules.dorm.entity.DormBuilding;
import com.dms.modules.dorm.service.DormBuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dorm/buildings")
@RequiredArgsConstructor
@SaCheckLogin
public class DormBuildingController {

    private final DormBuildingService dormBuildingService;

    @GetMapping("/list")
    public Result<List<DormBuilding>> list() {
        List<DormBuilding> list = dormBuildingService.list();
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<Page<DormBuilding>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name
    ) {
        Page<DormBuilding> page = new Page<>(current, size);

        LambdaQueryWrapper<DormBuilding> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            wrapper.like(DormBuilding::getName, name.trim());
        }

        Page<DormBuilding> result = dormBuildingService.page(page, wrapper);
        return Result.success(result);
    }

    @PostMapping
    public Result<?> save(@RequestBody @Valid DormBuildingAddRequestDTO dto) {
        DormBuilding entity = new DormBuilding();
        entity.setName(dto.getName());
        entity.setLocation(dto.getLocation());
        dormBuildingService.save(entity);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(
            @PathVariable Long id,
            @RequestBody @Valid DormBuildingUpdateRequestDTO dto
    ) {
        DormBuilding entity = dormBuildingService.getById(id);
        if (entity == null) {
            return Result.fail("楼栋不存在");
        }
        entity.setName(dto.getName());
        entity.setLocation(dto.getLocation());
        dormBuildingService.updateById(entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        dormBuildingService.removeById(id);
        return Result.success();
    }
}

