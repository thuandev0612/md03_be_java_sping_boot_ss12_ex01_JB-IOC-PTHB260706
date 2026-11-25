package com.example.baitapsession12.repository;

import com.example.baitapsession12.model.Supply;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
    List<Supply> findByDeletedFalseOrderByIdAsc();

    List<Supply> findByNameContainingIgnoreCaseAndDeletedFalseOrderByIdAsc(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Supply s WHERE s.id = :id AND s.deleted = false")
    Optional<Supply> findActiveByIdForUpdate(@Param("id") Long id);
}
