package com.healthkeeper.repository;

import com.healthkeeper.entity.CourseComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseCommentRepository extends JpaRepository<CourseComment, Long> {

    List<CourseComment> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<CourseComment> findByUserId(Long userId);

    List<CourseComment> findByCourseIdAndUserId(Long courseId, Long userId);
}
