package com.healthkeeper.repository;

import com.healthkeeper.entity.CourseChapter;
import com.healthkeeper.entity.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {

    List<CourseContent> findByChapterIdOrderByOrderAsc(Long chapterId);

    List<CourseContent> findByChapterIdAndOrderGreaterThanOrderByOrderAsc(Long chapterId, Integer order);

    List<CourseContent> findByChapterIdAndOrderLessThanOrderByOrderDesc(Long chapterId, Integer order);

    List<CourseContent> findByChapterIdAndIsFreeTrueOrderByOrderAsc(Long chapterId);
}
