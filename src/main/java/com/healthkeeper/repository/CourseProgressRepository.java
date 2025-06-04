package com.healthkeeper.repository;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseChapter;
import com.healthkeeper.entity.CourseContent;
import com.healthkeeper.entity.CourseProgress;
import com.healthkeeper.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    List<CourseProgress> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseProgress> findByUserId(Long userId);

    List<CourseProgress> findByUserIdAndStatus(Long userId, String status);

    List<CourseProgress> findByCourseId(Long courseId);

    List<CourseProgress> findByCourseIdAndStatus(Long courseId, String status);

    long countByCourseIdAndStatus(Long courseId, String status);

    List<CourseProgress> findByUserIdOrderByLastAccessedAtDesc(Long userId);

    List<CourseProgress> findByUserIdOrderByStartedAtDesc(Long userId);

    Page<CourseProgress> findByUserId(Long userId, Pageable pageable);

    Optional<CourseProgress> findByUserIdAndCourseIdAndChapterIdAndContentId(Long userId, Long courseId, Long chapterId, Long contentId);

    // 查找用户在特定时间范围内的课程进度
    List<CourseProgress> findByUserIdAndLastAccessedAtBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}