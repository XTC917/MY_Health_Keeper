package com.healthkeeper.controller;

import com.healthkeeper.dto.CourseRequest;
import com.healthkeeper.dto.CourseEnrollmentResponse;
import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseProgress;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseProgressRepository;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.repository.TrainingScheduleRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import com.healthkeeper.service.CourseService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CourseProgressRepository courseProgressRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createCourse(@Valid @RequestBody CourseRequest courseRequest,
                                        Authentication authentication) {
        try {
            Course course = courseService.createCourse(courseRequest, authentication.getName());
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Course>> getUserCourses(Authentication authentication) {
        List<Course> courses = courseService.getUserCourses(authentication.getName());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCourses(Authentication authentication) {
        try {
            System.out.println("Controller: Starting to fetch all courses...");
            List<Course> allCourses = courseService.getAllCourses();
            System.out.println("Controller: Found " + allCourses.size() + " courses");
            
            // 处理每个课程，确保没有循环引用
            List<Map<String, Object>> courseList = allCourses.stream().map(course -> {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("id", course.getId());
                courseMap.put("title", course.getTitle());
                courseMap.put("duration", course.getDuration());
                courseMap.put("targetAudience", course.getTargetAudience());
                courseMap.put("description", course.getDescription());
                courseMap.put("price", course.getPrice());
                courseMap.put("rating", course.getRating());
                courseMap.put("totalStudents", course.getTotalStudents());
                courseMap.put("status", course.getStatus());
                courseMap.put("thumbnail", course.getThumbnail());
                courseMap.put("videoUrl", course.getVideoUrl());
                courseMap.put("level", course.getLevel());
                courseMap.put("coverImage", course.getCoverImage());
                courseMap.put("content", course.getContent());
                courseMap.put("category", course.getCategory());
                courseMap.put("createdAt", course.getCreatedAt());
                courseMap.put("updatedAt", course.getUpdatedAt());
                
                // 处理创建者信息
                if (course.getCreatorId() != null) {
                    //Map<String, Object> creatorMap = new HashMap<>();
                    User user=userRepository.findById(course.getCreatorId()).orElseThrow(() -> new RuntimeException("User not found"));
                    courseMap.put("author", user.getUsername());
                    //creatorMap.put("id", course.getCreatorId());
//                    creatorMap.put("username", course.getCreator.getUsername());
//                    courseMap.put("creator", creatorMap);
                }
                if (authentication != null) {
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                    User user = userRepository.findById(userDetails.getId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    courseMap.put("isEnrolled", user.getEnrolledCourses().contains(course));
                } else {
                    courseMap.put("isEnrolled", false);
                }
                return courseMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(courseList);
        } catch (Exception e) {
            System.err.println("Error in getAllCourses: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching courses: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id, Authentication authentication) {
        try {
            Course course = courseService.getCourseById(id);
            
            // 将Course对象转换为Map
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", course.getId());
            courseMap.put("title", course.getTitle());
            courseMap.put("duration", course.getDuration());
            courseMap.put("targetAudience", course.getTargetAudience());
            courseMap.put("description", course.getDescription());
            courseMap.put("price", course.getPrice());
            courseMap.put("rating", course.getRating());
            courseMap.put("totalStudents", course.getTotalStudents());
            courseMap.put("status", course.getStatus());
            courseMap.put("thumbnail", course.getThumbnail());
            courseMap.put("videoUrl", course.getVideoUrl());
            courseMap.put("level", course.getLevel());
            courseMap.put("coverImage", course.getCoverImage());
            courseMap.put("content", course.getContent());
            courseMap.put("category", course.getCategory());
            courseMap.put("createdAt", course.getCreatedAt());
            courseMap.put("updatedAt", course.getUpdatedAt());
            
            // 处理创建者信息

            if (course.getCreatorId() != null) {
                User user=userRepository.findById(course.getCreatorId()).orElseThrow(() -> new RuntimeException("User not found"));
                courseMap.put("author", user.getUsername());
//                Map<String, Object> creatorMap = new HashMap<>();
//                creatorMap.put("id", course.getCreatorId());
//                Course course1= courseService.getCourseById(course.getCreatorId());
//                creatorMap.put("username", course1.getUsername());
//                courseMap.put("creator", creatorMap);

            }

            // 检查用户是否已加入课程
            if (authentication != null) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                courseMap.put("isEnrolled", user.getEnrolledCourses().contains(course));

                courseMap.put("liked", user.getLikedCourses().contains(course));
            } else {
                courseMap.put("isEnrolled", false);
                courseMap.put("liked", false);
            }
            courseMap.put("likeCount", course.getTotalLikeStudents());

            return ResponseEntity.ok(courseMap);
        } catch (Exception e) {
            System.err.println("Error in getCourseById: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching course: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course updatedCourse,
                                        Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to update this course");
        }

        // 更新课程信息
        if (updatedCourse.getTitle() != null) course.setTitle(updatedCourse.getTitle());
        if (updatedCourse.getDescription() != null) course.setDescription(updatedCourse.getDescription());
        if (updatedCourse.getCategory() != null) course.setCategory(updatedCourse.getCategory());
        if (updatedCourse.getLevel() != null) course.setLevel(updatedCourse.getLevel());
        if (updatedCourse.getDuration() != null) course.setDuration(updatedCourse.getDuration());
        if (updatedCourse.getPrice() != null) course.setPrice(updatedCourse.getPrice());
        if (updatedCourse.getCoverImage() != null) course.setCoverImage(updatedCourse.getCoverImage());
        if (updatedCourse.getVideoUrl() != null) course.setVideoUrl(updatedCourse.getVideoUrl());
        if (updatedCourse.getContent() != null) course.setContent(updatedCourse.getContent());

        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to delete this course");
        }

        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getLikedCourses().contains(course)) {
                user.getLikedCourses().remove(course);
                userRepository.save(user);
            }
            if (user.getEnrolledCourses().contains(course)){
                user.getEnrolledCourses().remove(course);
                userRepository.save(user);
            }
        }
        courseRepository.delete(course);
        return ResponseEntity.ok("Course deleted successfully");
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getCoursesByCategory(@PathVariable String category) {
        List<Course> courses = courseRepository.findByCategory(category);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<?> getCoursesByLevel(@PathVariable String level) {
        List<Course> courses = courseRepository.findByLevel(level);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCourses(@RequestParam String query) {
        List<Course> courses = courseRepository.findByTitleContainingIgnoreCase(query);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ResponseEntity<?> enrollCourse(@PathVariable Long id, Authentication authentication) {
        try {
            // 获取当前用户
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 获取目标课程
            Course course = courseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // 检查用户是否已经加入课程
            boolean isEnrolled = user.getEnrolledCourses().stream()
                    .anyMatch(c -> c.getId().equals(course.getId()));
            
            if (isEnrolled) {
                // 退出课程
                user.getEnrolledCourses().removeIf(c -> c.getId().equals(course.getId()));
                course.setTotalStudents(course.getTotalStudents() - 1);
            } else {
                // 加入课程
                user.getEnrolledCourses().add(course);
                course.setTotalStudents(course.getTotalStudents() + 1);
            }

            // 保存更改
            userRepository.save(user);
            courseRepository.save(course);

            // 返回简单的响应
            return ResponseEntity.ok(Map.of(
                "success", true,
                "enrolled", !isEnrolled,
                "totalStudents", course.getTotalStudents()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of(
                    "success", false,
                    "message", "Error enrolling in course: " + e.getMessage()
                ));
        }


    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ResponseEntity<?> toggleLike(@PathVariable Long id, Authentication authentication) {
        try {
            // 获取当前用户
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 获取课程
            Course course = courseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // 判断用户是否已点赞
            boolean alreadyLiked = user.getLikedCourses().stream()
                    .anyMatch(c -> c.getId().equals(course.getId()));

            if (alreadyLiked) {
                // 取消点赞
                user.getLikedCourses().removeIf(c -> c.getId().equals(course.getId()));
                course.setTotalLikeStudents(course.getTotalLikeStudents() - 1);
            } else {
                // 点赞
                user.getLikedCourses().add(course);
                course.setTotalLikeStudents(course.getTotalLikeStudents() + 1);
            }

            // 保存
            userRepository.save(user);
            courseRepository.save(course);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "liked", !alreadyLiked,
                    "likeCount", course.getTotalLikeStudents()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error toggling like: " + e.getMessage()
            ));
        }
    }


    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getEnrolledCourses(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Course> enrolledCourses = new ArrayList<>(user.getEnrolledCourses());
            
            // Process each course to avoid circular references
            List<Map<String, Object>> courseList = enrolledCourses.stream().map(course -> {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("id", course.getId());
                courseMap.put("title", course.getTitle());
                courseMap.put("duration", course.getDuration());
                courseMap.put("targetAudience", course.getTargetAudience());
                courseMap.put("description", course.getDescription());
                courseMap.put("price", course.getPrice());
                courseMap.put("rating", course.getRating());
                courseMap.put("totalStudents", course.getTotalStudents());
                courseMap.put("status", course.getStatus());
                courseMap.put("thumbnail", course.getThumbnail());
                courseMap.put("videoUrl", course.getVideoUrl());
                courseMap.put("level", course.getLevel());
                courseMap.put("coverImage", course.getCoverImage());
                courseMap.put("content", course.getContent());
                courseMap.put("category", course.getCategory());
                courseMap.put("createdAt", course.getCreatedAt());
                courseMap.put("updatedAt", course.getUpdatedAt());
                
                // Get course progress

                CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId())

                        .stream()
                        .findFirst()
                        .orElse(null);
                
                if (progress != null) {
                    Map<String, Object> progressMap = new HashMap<>();
                    progressMap.put("progress", progress.getProgress());
                    progressMap.put("status", progress.getStatus());
                    progressMap.put("lastAccessedAt", progress.getLastAccessedAt());
                    courseMap.put("progress", progressMap);
                }
                
                return courseMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(courseList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestBody Map<String, Object> progressData,
                                          Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 检查是否完成课程
        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course progress not found"));

        // 更新进度
        if (progressData.containsKey("progress")) {
            Integer progressValue = (Integer) progressData.get("progress");
            progress.setProgress(progressValue);
            if (progressValue >= 100) {
                progress.setStatus("COMPLETED");
                progress.setCompletedAt(LocalDateTime.now());
            }
        }

        // 更新学习时长
        if (progressData.containsKey("timeSpent")) {
            Integer timeSpent = (Integer) progressData.get("timeSpent");
            progress.setTotalTimeSpent(progress.getTotalTimeSpent() + timeSpent);
        }

        // 更新视频位置
        if (progressData.containsKey("lastPosition")) {
            Integer lastPosition = (Integer) progressData.get("lastPosition");
            progress.setLastPosition(lastPosition);
        }

        progress.setLastAccessedAt(LocalDateTime.now());
        CourseProgress savedProgress = courseProgressRepository.save(progress);
        return ResponseEntity.ok(savedProgress);
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCourseProgress(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 检查是否完成课程
        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course progress not found"));

        return ResponseEntity.ok(progress);
    }

    @GetMapping("/my-courses/status/{status}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyCoursesByStatus(@PathVariable String status, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseProgress> progressList = courseProgressRepository.findByUserIdAndStatus(user.getId(), status);
        return ResponseEntity.ok(progressList);
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCourseStudents(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User instructor = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getCreatorId().equals(instructor.getId())) {
            return ResponseEntity.badRequest().body("You don't have permission to view this course's students");
        }

        List<CourseProgress> progressList = courseProgressRepository.findByCourseId(course.getId());
        return ResponseEntity.ok(progressList);
    }

    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> rateCourse(@PathVariable Long id, @RequestBody Double rating,
                                      Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 检查是否完成课程
        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course progress not found"));

        if (!progress.getStatus().equals("COMPLETED")) {
            return ResponseEntity.badRequest().body("You must complete the course before rating");
        }

        // 更新课程评分
        course.setRating((course.getRating() * course.getTotalStudents() + rating) / (course.getTotalStudents() + 1));
        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<?> getInstructorCourses(@PathVariable Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        List<Course> courses = courseRepository.findByCreatorId(instructor.getId());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCourseStatistics(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseProgress> progressList = courseProgressRepository.findByUserId(user.getId());
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCourses", progressList.size());
        
        long completedCourses = progressList.stream()
                .filter(progress -> progress.getStatus().equals("COMPLETED"))
                .count();
        statistics.put("completedCourses", completedCourses);
        
        long inProgressCourses = progressList.stream()
                .filter(progress -> progress.getStatus().equals("IN_PROGRESS"))
                .count();
        statistics.put("inProgressCourses", inProgressCourses);
        
        double averageProgress = progressList.stream()
                .mapToInt(CourseProgress::getProgress)
                .average()
                .orElse(0.0);
        statistics.put("averageProgress", averageProgress);

        return ResponseEntity.ok(statistics);
    }
} 