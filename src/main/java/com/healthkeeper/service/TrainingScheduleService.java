/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me implement the functions of the back-end" 
 * and directly copied the code from its response.
 */
package com.healthkeeper.service;

import com.healthkeeper.dto.BatchTrainingScheduleRequest;
import com.healthkeeper.dto.TrainingScheduleDto;
import com.healthkeeper.dto.TrainingScheduleRequest;
import com.healthkeeper.dto.TrainingStatisticsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TrainingScheduleService {
    
    // 获取用户的所有训练计划
    Map<String, List<TrainingScheduleDto>> getUserSchedule();
    
    // 获取指定日期的训练计划
    List<TrainingScheduleDto> getDailySchedule(String date);
      // 添加训练计划项
    TrainingScheduleDto addScheduleItem(TrainingScheduleRequest request);
    
    // 批量添加训练计划项
    List<TrainingScheduleDto> addBatchScheduleItems(BatchTrainingScheduleRequest request);
    
    // 更新训练计划项
    TrainingScheduleDto updateScheduleItem(Long itemId, TrainingScheduleRequest request);
    
    // 删除训练计划项
    void deleteScheduleItem(Long itemId);
    
    // 批量获取日期范围内的训练计划
    Map<String, List<TrainingScheduleDto>> getScheduleRange(String startDate, String endDate);
    
    // 标记训练计划项为已完成/未完成
    TrainingScheduleDto updateCompletionStatus(Long itemId, boolean completed);
    
    // 获取训练统计数据
    TrainingStatisticsResponse getTrainingStatistics();
    
    // 获取指定日期的训练时长
    Integer getDailyTrainingDuration(LocalDate date);
}