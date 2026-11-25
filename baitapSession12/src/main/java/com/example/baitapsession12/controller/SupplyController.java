package com.example.baitapsession12.controller;

import com.example.baitapsession12.dto.*;
import com.example.baitapsession12.service.SupplyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/supplies")
public class SupplyController {
    private final SupplyService supplyService;

    @Autowired
    public SupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplyResponse>> create(@Valid @RequestBody SupplyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("SUCCESS", "Tạo vật tư thành công", supplyService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplyResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody SupplyUpdateRequest request) {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Cập nhật vật tư thành công", supplyService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplyResponse>>> findAll() {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Danh sách vật tư", supplyService.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SupplyResponse>>> search(@RequestParam("name") String name) {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Kết quả tìm kiếm", supplyService.search(name)));
    }

    @PatchMapping("/{id}/export")
    public ResponseEntity<ApiResponse<SupplyResponse>> exportStock(@PathVariable Long id,
                                                                 @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Xuất kho thành công", supplyService.exportStock(id, request.getAmount())));
    }

    @PatchMapping("/{id}/import")
    public ResponseEntity<ApiResponse<SupplyResponse>> importStock(@PathVariable Long id,
                                                                 @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Nhập kho thành công", supplyService.importStock(id, request.getAmount())));
    }

    @GetMapping("/statistics/daily-export")
    public ResponseEntity<ApiResponse<List<DailyExportResponse>>> dailyExport() {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Thống kê xuất kho trong ngày", supplyService.dailyExport()));
    }

    @GetMapping("/statistics/top-export")
    public ResponseEntity<ApiResponse<TopExportResponse>> topExport() {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Vật tư xuất kho nhiều nhất", supplyService.topExport()));
    }
}
