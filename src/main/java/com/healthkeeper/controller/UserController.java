package com.healthkeeper.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    HealthDataRepository healthDataRepository;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("address", user.getAddress());
        userInfo.put("gender", user.getGender());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("birthDate", user.getBirthDate());
        userInfo.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCurrentUser(@RequestBody User updatedUser, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 更新基本信息
        if (updatedUser.getUsername() != null) user.setUsername(updatedUser.getUsername());
        if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());
        if (updatedUser.getAddress() != null) user.setAddress(updatedUser.getAddress());
        if (updatedUser.getAvatar() != null) user.setAvatar(updatedUser.getAvatar());
        if (updatedUser.getGender() != null) user.setGender(updatedUser.getGender());
        if (updatedUser.getBirthDate() != null) user.setBirthDate(updatedUser.getBirthDate());
        if (updatedUser.getHeight() != null) user.setHeight(updatedUser.getHeight());
        if (updatedUser.getWeight() != null) user.setWeight(updatedUser.getWeight());

        // 如果提供了新密码，则更新密码
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(updatedUser.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/me/health-data")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyHealthData(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<HealthData> healthData = healthDataRepository.findByUserOrderByRecordedAtDesc(user);
        return ResponseEntity.ok(healthData);
    }

    @PostMapping("/me/health-data")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addHealthData(@RequestBody HealthData healthData, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        healthData.setUser(user);
        HealthData savedHealthData = healthDataRepository.save(healthData);
        return ResponseEntity.ok(savedHealthData);
    }

    @GetMapping("/me/health-data/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getHealthDataStatistics(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<HealthData> healthData = healthDataRepository.findByUserOrderByRecordedAtDesc(user);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 计算平均体重
        double avgWeight = healthData.stream()
                .map(HealthData::getWeight)
                .filter(w -> w != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        statistics.put("averageWeight", avgWeight);

        // 计算平均身高
        double avgHeight = healthData.stream()
                .map(HealthData::getHeight)
                .filter(h -> h != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        statistics.put("averageHeight", avgHeight);

        // 计算平均BMI
        double avgBMI = healthData.stream()
                .map(HealthData::getBmi)
                .filter(b -> b != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        statistics.put("averageBMI", avgBMI);

        // 计算平均心率
        double avgHeartRate = healthData.stream()
                .map(HealthData::getHeartRate)
                .filter(hr -> hr != null)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
        statistics.put("averageHeartRate", avgHeartRate);

        // 计算平均步数
        double avgSteps = healthData.stream()
                .map(HealthData::getSteps)
                .filter(s -> s != null)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
        statistics.put("averageSteps", avgSteps);

        // 计算平均睡眠时间
        double avgSleepTime = healthData.stream()
                .map(HealthData::getSleepTime)
                .filter(st -> st != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        statistics.put("averageSleepTime", avgSleepTime);

        // 计算平均卡路里消耗
        double avgCalories = healthData.stream()
                .map(HealthData::getCalories)
                .filter(c -> c != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        statistics.put("averageCalories", avgCalories);

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/me/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserStatistics(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> statistics = new HashMap<>();
        
        // 获取用户基本信息
        statistics.put("username", user.getUsername());
        statistics.put("email", user.getEmail());
        statistics.put("phone", user.getPhone());
        statistics.put("gender", user.getGender());
        statistics.put("birthDate", user.getBirthDate());
        statistics.put("address", user.getAddress());
        statistics.put("avatar", user.getAvatar());
        statistics.put("createdAt", user.getCreatedAt());

        // 获取健康数据统计
        List<HealthData> healthData = healthDataRepository.findByUserOrderByRecordedAtDesc(user);
        if (!healthData.isEmpty()) {
            HealthData latestHealthData = healthData.get(0);
            statistics.put("latestWeight", latestHealthData.getWeight());
            statistics.put("latestHeight", latestHealthData.getHeight());
            statistics.put("latestBMI", latestHealthData.getBmi());
            statistics.put("latestHeartRate", latestHealthData.getHeartRate());
            statistics.put("latestSteps", latestHealthData.getSteps());
            statistics.put("latestSleepTime", latestHealthData.getSleepTime());
            statistics.put("latestCalories", latestHealthData.getCalories());
        }

        return ResponseEntity.ok(statistics);
    }

    @PatchMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> updates, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 更新请求中提供的字段
        if (updates.containsKey("username")) {
            user.setUsername((String) updates.get("username"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("address")) {
            user.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("avatar")) {
            user.setAvatar((String) updates.get("avatar"));
        }
        if (updates.containsKey("gender")) {
            user.setGender((String) updates.get("gender"));
        }
        if (updates.containsKey("password") && updates.get("password") != null && !((String) updates.get("password")).isEmpty()) {
            user.setPassword(encoder.encode((String) updates.get("password")));
        }

        User savedUser = userRepository.save(user);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", savedUser);
        return ResponseEntity.ok(response);
    }
} 