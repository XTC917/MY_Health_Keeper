/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me implement the functions of the back-end" 
 * and directly copied the code from its response.
 */
package com.healthkeeper.controller;

import com.healthkeeper.dto.BatchTrainingScheduleRequest;
import com.healthkeeper.dto.TrainingScheduleDto;
import com.healthkeeper.dto.TrainingScheduleRequest;
import com.healthkeeper.dto.TrainingStatisticsResponse;
import com.healthkeeper.service.TrainingScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingScheduleService trainingScheduleService;

    @GetMapping("/schedule")
    public ResponseEntity<Map<String, List<TrainingScheduleDto>>> getUserSchedule() {
        return ResponseEntity.ok(trainingScheduleService.getUserSchedule());
    }

    @GetMapping("/schedule/{date}")
    public ResponseEntity<List<TrainingScheduleDto>> getDailySchedule(@PathVariable String date) {
        return ResponseEntity.ok(trainingScheduleService.getDailySchedule(date));
    }    @PostMapping("/schedule")
    public ResponseEntity<TrainingScheduleDto> addScheduleItem(@RequestBody TrainingScheduleRequest request) {
        return new ResponseEntity<>(trainingScheduleService.addScheduleItem(request), HttpStatus.CREATED);
    }

    @PostMapping("/schedule/batch")
    public ResponseEntity<List<TrainingScheduleDto>> addBatchScheduleItems(@RequestBody BatchTrainingScheduleRequest request) {
        return new ResponseEntity<>(trainingScheduleService.addBatchScheduleItems(request), HttpStatus.CREATED);
    }

    @PutMapping("/schedule/item/{itemId}")
    public ResponseEntity<TrainingScheduleDto> updateScheduleItem(
            @PathVariable Long itemId,
            @RequestBody TrainingScheduleRequest request) {
        return ResponseEntity.ok(trainingScheduleService.updateScheduleItem(itemId, request));
    }

    @DeleteMapping("/schedule/item/{itemId}")
    public ResponseEntity<Void> deleteScheduleItem(@PathVariable Long itemId) {
        trainingScheduleService.deleteScheduleItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedule/range")
    public ResponseEntity<Map<String, List<TrainingScheduleDto>>> getScheduleRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(trainingScheduleService.getScheduleRange(startDate, endDate));
    }

    @PutMapping("/schedule/item/{itemId}/complete")
    public ResponseEntity<TrainingScheduleDto> updateCompletionStatus(
            @PathVariable Long itemId,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean completed = requestBody.getOrDefault("completed", false);
        return ResponseEntity.ok(trainingScheduleService.updateCompletionStatus(itemId, completed));
    }
    
    // 获取训练统计数据
    @GetMapping("/statistics")
    public ResponseEntity<TrainingStatisticsResponse> getTrainingStatistics() {
        return ResponseEntity.ok(trainingScheduleService.getTrainingStatistics());
    }
    
    // 获取指定日期的训练时长
    @GetMapping("/daily-duration/{date}")
    public ResponseEntity<Integer> getDailyTrainingDuration(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(trainingScheduleService.getDailyTrainingDuration(localDate));
    }
}