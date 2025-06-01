package com.healthkeeper.controller;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseChapter;
import com.healthkeeper.entity.CourseContent;
import com.healthkeeper.entity.CourseProgress;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseChapterRepository;
import com.healthkeeper.repository.CourseContentRepository;
import com.healthkeeper.repository.CourseProgressRepository;
import com.healthkeeper.repository.CourseRepository;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/course-progress")
public class CourseProgressController {
    @Autowired
    CourseProgressRepository courseProgressRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CourseChapterRepository courseChapterRepository;

    @Autowired
    CourseContentRepository courseContentRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createProgress(@RequestBody CourseProgress progress, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(progress.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseChapter chapter = courseChapterRepository.findById(progress.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        CourseContent content = courseContentRepository.findById(progress.getContentId())
                .orElseThrow(() -> new RuntimeException("Content not found"));

        // 检查是否已经存在进度记录
        CourseProgress existingProgress = courseProgressRepository.findByUserIdAndCourseIdAndChapterIdAndContentId(
                user.getId(), course.getId(), chapter.getId(), content.getId()).orElse(null);

        if (existingProgress != null) {
            // 更新现有进度
            existingProgress.setProgress(progress.getProgress());
            existingProgress.setLastAccessedAt(LocalDateTime.now());
            CourseProgress savedProgress = courseProgressRepository.save(existingProgress);
            return ResponseEntity.ok(savedProgress);
        } else {
            // 创建新进度
            progress.setUserId(user.getId());
            progress.setLastAccessedAt(LocalDateTime.now());
            CourseProgress savedProgress = courseProgressRepository.save(progress);
            return ResponseEntity.ok(savedProgress);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyProgress(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastAccessedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<CourseProgress> progress = courseProgressRepository.findByUserId(user.getId(), pageable);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/me/course/{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyCourseProgress(@PathVariable Long courseId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<CourseProgress> progress = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        
        // 计算课程总体进度
        Map<String, Object> response = new HashMap<>();
        response.put("progress", progress);
        
        // 计算章节完成情况
        List<CourseChapter> chapters = courseChapterRepository.findByCourseIdOrderByOrderAsc(course.getId());
        Map<Long, Double> chapterProgress = new HashMap<>();
        
        for (CourseChapter chapter : chapters) {
            List<CourseProgress> chapterProgressList = progress.stream()
                    .filter(p -> p.getChapterId().equals(chapter.getId()))
                    .collect(Collectors.toList());
            
            if (!chapterProgressList.isEmpty()) {
                double averageProgress = chapterProgressList.stream()
                        .mapToDouble(CourseProgress::getProgress)
                        .average()
                        .orElse(0.0);
                chapterProgress.put(chapter.getId(), averageProgress);
            } else {
                chapterProgress.put(chapter.getId(), 0.0);
            }
        }
        
        response.put("chapterProgress", chapterProgress);
        
        // 计算总体完成度
        double totalProgress = chapterProgress.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        response.put("totalProgress", totalProgress);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyLearningStatistics(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseProgress> allProgress = courseProgressRepository.findByUserId(user.getId());
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 计算总学习时长
        long totalLearningTime = allProgress.stream()
                .mapToLong(progress -> progress.getLastAccessedAt().toLocalTime().toSecondOfDay())
                .sum();
        statistics.put("totalLearningTime", totalLearningTime);
        
        // 计算完成课程数
        long completedCourses = allProgress.stream()
                .filter(progress -> progress.getProgress() >= 100)
                .map(CourseProgress::getCourseId)
                .distinct()
                .count();
        statistics.put("completedCourses", completedCourses);
        
        // 计算进行中的课程数
        long inProgressCourses = allProgress.stream()
                .filter(progress -> progress.getProgress() > 0 && progress.getProgress() < 100)
                .map(CourseProgress::getCourseId)
                .distinct()
                .count();
        statistics.put("inProgressCourses", inProgressCourses);
        
        // 计算最近学习时间
        LocalDateTime lastLearningTime = allProgress.stream()
                .map(CourseProgress::getLastAccessedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        statistics.put("lastLearningTime", lastLearningTime);
        
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProgress(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastAccessedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<CourseProgress> progress = courseProgressRepository.findAll(pageable);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/admin/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCourseProgress(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<CourseProgress> progress = courseProgressRepository.findByCourseId(course.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("progress", progress);
        
        // 计算课程统计信息
        double averageProgress = progress.stream()
                .mapToDouble(CourseProgress::getProgress)
                .average()
                .orElse(0.0);
        response.put("averageProgress", averageProgress);
        
        long totalLearners = progress.stream()
                .map(CourseProgress::getUserId)
                .distinct()
                .count();
        response.put("totalLearners", totalLearners);
        
        long completedLearners = progress.stream()
                .filter(p -> p.getProgress() >= 100)
                .map(CourseProgress::getUserId)
                .distinct()
                .count();
        response.put("completedLearners", completedLearners);
        
        return ResponseEntity.ok(response);
    }
} 