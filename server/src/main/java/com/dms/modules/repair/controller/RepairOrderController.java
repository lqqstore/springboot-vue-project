package com.dms.modules.repair.controller;

import com.dms.common.Result;
import com.dms.modules.repair.dto.RepairOrderAddRequestDTO;
import com.dms.modules.repair.dto.RepairOrderUpdateRequestDTO;
import com.dms.modules.repair.entity.RepairOrder;
import com.dms.modules.repair.service.RepairOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairOrderController {
    
    private final RepairOrderService repairOrderService;
    
    @GetMapping("/list")
    public Result<List<RepairOrder>> getRepairOrderList() {
        List<RepairOrder> repairOrders = repairOrderService.getRepairOrderList();
        return Result.success(repairOrders);
    }
    
    @GetMapping("/student/{studentId}")
    public Result<List<RepairOrder>> getRepairOrdersByStudentId(@PathVariable Long studentId) {
        List<RepairOrder> repairOrders = repairOrderService.getRepairOrdersByStudentId(studentId);
        return Result.success(repairOrders);
    }
    
    @GetMapping("/{id}")
    public Result<RepairOrder> getRepairOrderById(@PathVariable Long id) {
        RepairOrder repairOrder = repairOrderService.getRepairOrderById(id);
        return Result.success(repairOrder);
    }
    
    @PostMapping
    public Result<RepairOrder> addRepairOrder(@RequestBody @Valid RepairOrderAddRequestDTO dto) {
        RepairOrder repairOrder = repairOrderService.addRepairOrder(dto);
        return Result.success(repairOrder);
    }
    
    @PutMapping("/{id}")
    public Result<RepairOrder> updateRepairOrder(@PathVariable Long id, @RequestBody @Valid RepairOrderUpdateRequestDTO dto) {
        RepairOrder repairOrder = repairOrderService.updateRepairOrder(id, dto);
        return Result.success(repairOrder);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteRepairOrder(@PathVariable Long id) {
        repairOrderService.deleteRepairOrder(id);
        return Result.success();
    }
}
