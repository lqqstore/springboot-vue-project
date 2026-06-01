package com.dms.modules.repair.controller;

import com.dms.common.Result;
import com.dms.modules.repair.dto.RepairOrderAddRequestDTO;
import com.dms.modules.repair.dto.RepairOrderUpdateRequestDTO;
import com.dms.modules.repair.dto.RepairOrderVO;
import com.dms.modules.repair.entity.RepairOrder;
import com.dms.modules.repair.service.RepairOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @GetMapping("/list")
    public Result<List<RepairOrderVO>> getRepairOrderList() {
        return Result.success(repairOrderService.getRepairOrderList());
    }

    @GetMapping("/{id}")
    public Result<RepairOrderVO> getRepairOrderById(@PathVariable Long id) {
        return Result.success(repairOrderService.getRepairOrderById(id));
    }

    @PostMapping
    public Result<RepairOrder> addRepairOrder(@RequestBody @Valid RepairOrderAddRequestDTO dto) {
        return Result.success(repairOrderService.addRepairOrder(dto));
    }

    @PutMapping("/{id}")
    public Result<RepairOrder> updateRepairOrder(@PathVariable Long id, @RequestBody @Valid RepairOrderUpdateRequestDTO dto) {
        return Result.success(repairOrderService.updateRepairOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRepairOrder(@PathVariable Long id) {
        repairOrderService.deleteRepairOrder(id);
        return Result.success();
    }
}
