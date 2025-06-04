package com.healthkeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStatisticsResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrainingTime {
        private LocalDate date;
        private Integer totalMinutes;
        private Integer courseMinutes;
        private Integer customTrainingMinutes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyTrainingTime {
        private String week; // "2024-W01" format
        private Integer totalMinutes;
        private Integer courseMinutes;
        private Integer customTrainingMinutes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrainingTime {
        private String month; // "2024-01" format
        private Integer totalMinutes;
        private Integer courseMinutes;
        private Integer customTrainingMinutes;
    }
    
    // 今日训练时长
    private DailyTrainingTime todayTraining;
    
    // 近7天每日训练时长
    private List<DailyTrainingTime> last7Days;
    
    // 近12周每周训练时长  
    private List<WeeklyTrainingTime> last12Weeks;
    
    // 近12个月每月训练时长
    private List<MonthlyTrainingTime> last12Months;
}