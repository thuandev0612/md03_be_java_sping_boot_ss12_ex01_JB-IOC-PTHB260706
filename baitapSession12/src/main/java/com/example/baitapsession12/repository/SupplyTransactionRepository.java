package com.example.baitapsession12.repository;

import com.example.baitapsession12.dto.DailyExportResponse;
import com.example.baitapsession12.dto.TopExportResponse;
import com.example.baitapsession12.model.SupplyTransaction;
import com.example.baitapsession12.model.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SupplyTransactionRepository extends JpaRepository<SupplyTransaction, Long> {
    @Query("""
            SELECT new com.example.baitapsession12.dto.DailyExportResponse(s.id, s.name, SUM(t.amount))
            FROM SupplyTransaction t JOIN t.supply s
            WHERE t.type = :type AND t.occurredAt >= :start AND t.occurredAt < :end
            GROUP BY s.id, s.name
            ORDER BY SUM(t.amount) DESC, s.id ASC
            """)
    List<DailyExportResponse> findDailyExports(@Param("type") TransactionType type,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("""
            SELECT new com.example.baitapsession12.dto.TopExportResponse(s.id, s.name, SUM(t.amount))
            FROM SupplyTransaction t JOIN t.supply s
            WHERE t.type = :type
            GROUP BY s.id, s.name
            ORDER BY SUM(t.amount) DESC, s.id ASC
            """)
    List<TopExportResponse> findTopExports(@Param("type") TransactionType type, Pageable pageable);
}
