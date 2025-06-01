/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me implement the functions of the back-end" 
 * and directly copied the code from its response.
 */
package com.healthkeeper.repository;

import com.healthkeeper.entity.TrainingSchedule;
import com.healthkeeper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingScheduleRepository extends JpaRepository<TrainingSchedule, Long> {
    
    // 查找用户的所有训练计划
    List<TrainingSchedule> findByUser(User user);
    
    // 查找用户特定日期的训练计划
    List<TrainingSchedule> findByUserAndDate(User user, LocalDate date);
    
    // 查找用户特定日期范围内的训练计划
    List<TrainingSchedule> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    // 根据用户和课程ID查找训练计划
    List<TrainingSchedule> findByUserAndCourse_Id(User user, Long courseId);
} 