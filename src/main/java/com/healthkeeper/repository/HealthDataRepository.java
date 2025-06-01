package com.healthkeeper.repository;

import com.healthkeeper.entity.HealthData;
import com.healthkeeper.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthDataRepository extends JpaRepository<HealthData, Long> {
    List<HealthData> findByUserOrderByRecordedAtDesc(User user);
    List<HealthData> findByUserAndRecordedAtBetweenOrderByRecordedAtDesc(
        User user, LocalDateTime startDate, LocalDateTime endDate);
    
    Page<HealthData> findByUser(User user, Pageable pageable);
    
    Optional<HealthData> findFirstByUserOrderByRecordedAtDesc(User user);
    
    @Query("SELECT AVG(h.weight) FROM HealthData h WHERE h.user = ?1 AND h.recordedAt >= ?2")
    Double findAverageWeightByUserAndDateAfter(User user, LocalDateTime date);
    
    @Query("SELECT AVG(h.height) FROM HealthData h WHERE h.user = ?1 AND h.recordedAt >= ?2")
    Double findAverageHeightByUserAndDateAfter(User user, LocalDateTime date);
    
    @Query("SELECT AVG(h.heartRate) FROM HealthData h WHERE h.user = ?1 AND h.recordedAt >= ?2")
    Double findAverageHeartRateByUserAndDateAfter(User user, LocalDateTime date);
    
    @Query("SELECT AVG(h.steps) FROM HealthData h WHERE h.user = ?1 AND h.recordedAt >= ?2")
    Double findAverageStepsByUserAndDateAfter(User user, LocalDateTime date);
    
    @Query("SELECT AVG(h.calories) FROM HealthData h WHERE h.user = ?1 AND h.recordedAt >= ?2")
    Double findAverageCaloriesByUserAndDateAfter(User user, LocalDateTime date);
} 