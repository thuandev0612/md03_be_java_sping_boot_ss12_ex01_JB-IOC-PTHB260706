package com.example.baitapsession12.service;

import com.example.baitapsession12.dto.*;
import com.example.baitapsession12.exception.ApiException;
import com.example.baitapsession12.model.Supply;
import com.example.baitapsession12.model.SupplyTransaction;
import com.example.baitapsession12.model.TransactionType;
import com.example.baitapsession12.repository.SupplyRepository;
import com.example.baitapsession12.repository.SupplyTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SupplyService {
    private static final Logger HISTORY = LoggerFactory.getLogger("inventory.history");
    private final SupplyRepository supplyRepository;
    private final SupplyTransactionRepository transactionRepository;

    @Autowired
    public SupplyService(SupplyRepository supplyRepository, SupplyTransactionRepository transactionRepository) {
        this.supplyRepository = supplyRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public SupplyResponse create(SupplyCreateRequest request) {
        Supply supply = new Supply();
        supply.setName(request.getName());
        supply.setSpecification(request.getSpecification());
        supply.setProvider(request.getProvider());
        supply.setUnit(request.getUnit());
        supply.setQuantity(0);
        supply.setDeleted(false);
        Supply saved = supplyRepository.saveAndFlush(supply);
        log.info("Đã tạo mới vật tư: {} với ID: {}", saved.getName(), saved.getId());
        return SupplyResponse.from(saved);
    }

    @Transactional
    public SupplyResponse update(Long id, SupplyUpdateRequest request) {
        log.info("Cập nhật thông tin vật tư ID {}", id);
        Supply supply = findActiveForUpdate(id);
        supply.setName(request.getName());
        supply.setSpecification(request.getSpecification());
        supply.setProvider(request.getProvider());
        return SupplyResponse.from(supplyRepository.save(supply));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Xóa mềm vật tư ID {}", id);
        Supply supply = findActiveForUpdate(id);
        supply.setDeleted(true);
        supplyRepository.save(supply);
    }

    @Transactional(readOnly = true)
    public List<SupplyResponse> findAll() {
        log.info("Lấy danh sách vật tư đang hoạt động");
        List<SupplyResponse> result = supplyRepository.findByDeletedFalseOrderByIdAsc().stream()
                .map(SupplyResponse::from).toList();
        log.debug("Đã truy vấn {} vật tư đang hoạt động", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<SupplyResponse> search(String name) {
        String keyword = name.trim();
        log.info("Tìm kiếm vật tư theo tên: {}", keyword);
        List<SupplyResponse> result = supplyRepository
                .findByNameContainingIgnoreCaseAndDeletedFalseOrderByIdAsc(keyword).stream()
                .map(SupplyResponse::from).toList();
        if (result.isEmpty()) {
            log.info("Không tìm thấy vật tư nào khớp với từ khóa: {}", keyword);
        }
        log.debug("Tìm thấy {} vật tư với từ khóa {}", result.size(), keyword);
        return result;
    }

    @Transactional
    public SupplyResponse importStock(Long id, long amount) {
        log.info("Tiếp nhận yêu cầu nhập kho ID {}, số lượng {}", id, amount);
        Supply supply = findActiveForUpdate(id);
        long previous = supply.getQuantity();
        long updated;
        try {
            updated = Math.addExact(previous, amount);
        } catch (ArithmeticException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Số lượng nhập vượt giới hạn cho phép");
        }
        supply.setQuantity(updated);
        supplyRepository.save(supply);
        saveTransaction(supply, TransactionType.IMPORT, amount);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                HISTORY.info("Nhập kho ID {}, số lượng +{}, tồn cũ {}", id, amount, previous);
                log.info("Đã nhập kho ID {}, tồn mới {}", id, updated);
            }
        });
        return SupplyResponse.from(supply);
    }

    @Transactional
    public SupplyResponse exportStock(Long id, long amount) {
        log.info("Tiếp nhận yêu cầu xuất kho ID {}, số lượng {}", id, amount);
        Supply supply = findActiveForUpdate(id);
        long previous = supply.getQuantity();
        if (previous < amount) {
            ApiException exception = new ApiException(HttpStatus.BAD_REQUEST,
                    "Số lượng tồn kho không đủ để xuất");
            log.error("Thất bại khi xuất kho ID {}: Yêu cầu {}, hiện có {}", id, amount, previous, exception);
            throw exception;
        }
        supply.setQuantity(previous - amount);
        supplyRepository.save(supply);
        saveTransaction(supply, TransactionType.EXPORT, amount);
        log.info("Đã xuất kho ID {}, số lượng {}, tồn mới {}", id, amount, supply.getQuantity());
        return SupplyResponse.from(supply);
    }

    @Transactional(readOnly = true)
    public List<DailyExportResponse> dailyExport() {
        LocalDateTime startedAt = LocalDateTime.now();
        long startedNanos = System.nanoTime();
        log.info("Bắt đầu thống kê xuất kho trong ngày lúc {}", startedAt);
        LocalDate today = startedAt.toLocalDate();
        List<DailyExportResponse> result = transactionRepository.findDailyExports(TransactionType.EXPORT,
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        log.info("Hoàn thành thống kê xuất kho lúc {}, thời gian {} ms", LocalDateTime.now(),
                (System.nanoTime() - startedNanos) / 1_000_000);
        return result;
    }

    @Transactional(readOnly = true)
    public TopExportResponse topExport() {
        log.info("Thống kê vật tư xuất kho nhiều nhất");
        return transactionRepository.findTopExports(TransactionType.EXPORT, PageRequest.of(0, 1)).stream()
                .findFirst().orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Chưa có dữ liệu giao dịch để thống kê"));
    }

    private Supply findActiveForUpdate(Long id) {
        return supplyRepository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy vật tư đang hoạt động với ID " + id));
    }

    private void saveTransaction(Supply supply, TransactionType type, long amount) {
        SupplyTransaction transaction = new SupplyTransaction();
        transaction.setSupply(supply);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setOccurredAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
