package com.healthkeeper.repository;

import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseChapterRepository extends JpaRepository<CourseChapter, Long> {
    List<CourseChapter> findByCourseIdOrderByOrderAsc(Long courseId);
    
    List<CourseChapter> findByCourseIdAndOrderGreaterThanOrderByOrderAsc(Long courseId, Integer order);
    
    List<CourseChapter> findByCourseIdAndOrderLessThanOrderByOrderDesc(Long courseId, Integer order);
} 