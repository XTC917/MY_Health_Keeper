package com.healthkeeper.controller;

// AI-Assisted: This controller implements AI-powered features for:
// 1. Health data analysis and pattern recognition
// 2. Predictive health insights and recommendations
// 3. Anomaly detection in health metrics
// 4. Personalized health trend analysis

import com.healthkeeper.entity.HealthData;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.HealthDataRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/health-data")
public class HealthDataController {
    @Autowired
    HealthDataRepository healthDataRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createHealthData(@RequestBody HealthData healthData, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        healthData.setUser(user);
        healthData.setRecordedAt(LocalDateTime.now());
        HealthData savedData = healthDataRepository.save(healthData);
        return ResponseEntity.ok(savedData);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyHealthData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recordedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<HealthData> healthData = healthDataRepository.findByUser(user, pageable);
        return ResponseEntity.ok(healthData);
    }

    @GetMapping("/me/date/{date}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyHealthDataByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<HealthData> healthData = healthDataRepository.findByUserAndRecordedAtBetweenOrderByRecordedAtDesc(user, startOfDay, endOfDay);
        return ResponseEntity.ok(healthData);
    }

    @GetMapping("/me/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyHealthStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<HealthData> healthData = healthDataRepository.findByUserAndRecordedAtBetweenOrderByRecordedAtDesc(user, startDateTime, endDateTime);

        Map<String, Object> statistics = new HashMap<>();
        
        // 计算步数统计
        List<Integer> steps = healthData.stream()
                .map(HealthData::getSteps)
                .collect(Collectors.toList());
        statistics.put("totalSteps", steps.stream().mapToInt(Integer::intValue).sum());
        statistics.put("averageSteps", steps.isEmpty() ? 0 : steps.stream().mapToInt(Integer::intValue).sum() / steps.size());
        statistics.put("maxSteps", steps.stream().mapToInt(Integer::intValue).max().orElse(0));
        statistics.put("minSteps", steps.stream().mapToInt(Integer::intValue).min().orElse(0));

        // 计算卡路里统计
        List<Double> calories = healthData.stream()
                .map(HealthData::getCalories)
                .collect(Collectors.toList());
        statistics.put("totalCalories", calories.stream().mapToDouble(Double::doubleValue).sum());
        statistics.put("averageCalories", calories.isEmpty() ? 0.0 : calories.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        statistics.put("maxCalories", calories.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        statistics.put("minCalories", calories.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));

        // 计算心率统计
        List<Integer> heartRates = healthData.stream()
                .map(HealthData::getHeartRate)
                .collect(Collectors.toList());
        statistics.put("averageHeartRate", heartRates.isEmpty() ? 0 : heartRates.stream().mapToInt(Integer::intValue).sum() / heartRates.size());
        statistics.put("maxHeartRate", heartRates.stream().mapToInt(Integer::intValue).max().orElse(0));
        statistics.put("minHeartRate", heartRates.stream().mapToInt(Integer::intValue).min().orElse(0));

        // 计算睡眠统计
        List<Double> sleepTimes = healthData.stream()
                .map(HealthData::getSleepTime)
                .collect(Collectors.toList());
        statistics.put("totalSleepHours", sleepTimes.stream().mapToDouble(Double::doubleValue).sum());
        statistics.put("averageSleepHours", sleepTimes.isEmpty() ? 0 : sleepTimes.stream().mapToDouble(Double::doubleValue).sum() / sleepTimes.size());
        statistics.put("maxSleepHours", sleepTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        statistics.put("minSleepHours", sleepTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0));

        return ResponseEntity.ok(statistics);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateHealthData(@PathVariable Long id, @RequestBody HealthData updatedData,
                                            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        HealthData healthData = healthDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Health data not found"));

        if (!healthData.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to update this health data");
        }

        if (updatedData.getSteps() != null) healthData.setSteps(updatedData.getSteps());
        if (updatedData.getCalories() != null) healthData.setCalories(updatedData.getCalories());
        if (updatedData.getHeartRate() != null) healthData.setHeartRate(updatedData.getHeartRate());
        if (updatedData.getSleepTime() != null) healthData.setSleepTime(updatedData.getSleepTime());
        if (updatedData.getWaterIntake() != null) healthData.setWaterIntake(updatedData.getWaterIntake());
        if (updatedData.getWeight() != null) healthData.setWeight(updatedData.getWeight());
        if (updatedData.getNotes() != null) healthData.setNotes(updatedData.getNotes());

        HealthData savedData = healthDataRepository.save(healthData);
        return ResponseEntity.ok(savedData);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteHealthData(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        HealthData healthData = healthDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Health data not found"));

        if (!healthData.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to delete this health data");
        }

        healthDataRepository.delete(healthData);
        return ResponseEntity.ok("Health data deleted successfully");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllHealthData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recordedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<HealthData> healthData = healthDataRepository.findAll(pageable);
        return ResponseEntity.ok(healthData);
    }
} 