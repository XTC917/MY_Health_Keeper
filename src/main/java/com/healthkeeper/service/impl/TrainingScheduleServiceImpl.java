/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me implement the functions of the back-end" 
 * and directly copied the code from its response.
 */
package com.healthkeeper.service.impl;

import com.healthkeeper.dto.BatchTrainingScheduleRequest;
import com.healthkeeper.dto.TrainingScheduleDto;
import com.healthkeeper.dto.TrainingScheduleRequest;
import com.healthkeeper.dto.TrainingStatisticsResponse;
import com.healthkeeper.entity.Course;
import com.healthkeeper.entity.CourseProgress;
import com.healthkeeper.entity.TrainingSchedule;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.CourseProgressRepository;
import com.healthkeeper.repository.CourseRepository;
import com.healthkeeper.repository.TrainingScheduleRepository;
import com.healthkeeper.security.SecurityUtil;
import com.healthkeeper.service.TrainingScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingScheduleServiceImpl implements TrainingScheduleService {    
    private final TrainingScheduleRepository trainingScheduleRepository;
    private final CourseRepository courseRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final SecurityUtil securityUtil;
    
    // 使用灵活的日期格式，能同时处理yyyy-M-d和yyyy-MM-dd格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
      // 尝试用多种格式解析日期
    private LocalDate parseDate(String dateString) {
        System.out.println("尝试解析日期: " + dateString);
        
        // 定义多种可能的日期格式
        DateTimeFormatter[] formatters = {
            DATE_FORMATTER,                           // yyyy-MM-dd
            DateTimeFormatter.ofPattern("yyyy-M-d"),  // yyyy-M-d
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),// yyyy/MM/dd
            DateTimeFormatter.ofPattern("yyyy/M/d")   // yyyy/M/d
        };
        
        // 检查日期字符串是否包含分隔符，如果不包含则可能是数字格式
        if (dateString != null && !dateString.contains("-") && !dateString.contains("/")) {
            try {
                // 尝试处理可能的时间戳
                long timestamp = Long.parseLong(dateString);
                return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (NumberFormatException e) {
                System.out.println("日期字符串不是有效的时间戳: " + e.getMessage());
            }
        }
        
        // 尝试所有可能的格式
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate date = LocalDate.parse(dateString, formatter);
                System.out.println("成功解析日期: " + date);
                return date;
            } catch (Exception e) {
                System.out.println("使用格式 " + formatter + " 解析失败: " + e.getMessage());
            }
        }
          // 如果上面的方法都失败了，尝试手动解析 yyyy-M-d 格式
        if (dateString != null) {
            try {
                String[] parts = dateString.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    
                    LocalDate date = LocalDate.of(year, month, day);
                    System.out.println("手动解析日期成功: " + date);
                    return date;
                }
            } catch (Exception e) {
                System.out.println("手动解析日期失败: " + e.getMessage());
            }
        }
        
        throw new RuntimeException("无法解析日期: " + dateString + "，请使用格式 yyyy-MM-dd");
    }

    @Override
    public Map<String, List<TrainingScheduleDto>> getUserSchedule() {
        User currentUser = securityUtil.getCurrentUser();
        List<TrainingSchedule> schedules = trainingScheduleRepository.findByUser(currentUser);
        
        return schedules.stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getDate().format(DATE_FORMATTER),
                        Collectors.mapping(this::convertToDto, Collectors.toList())
                ));
    }

    @Override
    public List<TrainingScheduleDto> getDailySchedule(String date) {
        User currentUser = securityUtil.getCurrentUser();
        LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
        List<TrainingSchedule> schedules = trainingScheduleRepository.findByUserAndDate(currentUser, localDate);
        
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }    @Override
    @Transactional
    public TrainingScheduleDto addScheduleItem(TrainingScheduleRequest request) {
        try {
            // 增加详细日志输出，便于调试
            System.out.println("接收到添加训练计划请求: " + request);
            System.out.println("课程ID: " + request.getCourseId());
            System.out.println("日期: " + request.getDate());
            System.out.println("开始时间: " + request.getStartTime());
            
            // 获取当前用户，添加错误处理
            User currentUser;
            try {
                currentUser = securityUtil.getCurrentUser();
                System.out.println("当前用户ID: " + currentUser.getId());
            } catch (Exception e) {
                System.err.println("获取当前用户失败: " + e.getMessage());
                throw new RuntimeException("用户认证失败或会话已过期，请重新登录", e);
            }
              // 查找课程，添加错误处理
            Course course;
            try {
                Long courseId = request.getCourseId();
                if (courseId == null) {
                    throw new RuntimeException("课程ID不能为空");
                }
                
                System.out.println("尝试查找课程ID: " + courseId + ", 类型: " + courseId.getClass().getName());
                course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("课程不存在，ID: " + courseId));
                System.out.println("找到课程: " + course.getTitle());
            } catch (NumberFormatException e) {
                System.err.println("课程ID格式不正确: " + e.getMessage());
                throw new RuntimeException("课程ID必须是数字", e);
            } catch (Exception e) {
                System.err.println("查找课程失败: " + e.getMessage());
                throw new RuntimeException("无法找到指定课程: " + e.getMessage(), e);
            }
            
            // 解析日期和时间，添加错误处理
            LocalDate date;
            LocalTime startTime;
            try {
                date = parseDate(request.getDate());
                System.out.println("解析后的日期: " + date);
                
                startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
                System.out.println("解析后的时间: " + startTime);
            } catch (Exception e) {
                System.err.println("解析日期或时间失败: " + e.getMessage());
                throw new RuntimeException("日期或时间格式不正确", e);
            }
              // 额外检查日期是否为空
            if (date == null) {
                throw new RuntimeException("日期解析后为空，无法保存训练计划");
            }
            
            // 确保时间不为空
            if (startTime == null) {
                throw new RuntimeException("时间解析后为空，无法保存训练计划");
            }
            
            // 创建训练计划对象前再次检查字段
            System.out.println("准备构建训练计划对象:");
            System.out.println("- 用户: " + (currentUser != null ? currentUser.getId() : "null"));
            System.out.println("- 课程: " + (course != null ? course.getId() : "null"));
            System.out.println("- 日期: " + date);
            System.out.println("- 时间: " + startTime);
              // 创建并保存训练计划
            LocalDate today = LocalDate.now();
            TrainingSchedule schedule = TrainingSchedule.builder()
                    .user(currentUser)
                    .course(course)
                    .date(date)        // schedule_date字段
                    .dateOriginal(date) // date字段，与schedule_date保持一致
                    .startTime(startTime)
                    .completed(false)
                    .createdAt(today)
                    .updatedAt(today)   // 添加updatedAt字段赋值
                    .build();
            
            // 再次验证对象状态
            if (schedule.getDate() == null) {
                throw new RuntimeException("构建训练计划对象后发现日期为空，字段映射可能有问题");
            }
            
            System.out.println("准备保存训练计划: " + schedule);
            
            // 在SQL执行前确认实体状态
            System.out.println("检查训练计划对象字段值:");
            System.out.println("- schedule.getId(): " + schedule.getId());
            System.out.println("- schedule.getUser().getId(): " + schedule.getUser().getId());
            System.out.println("- schedule.getCourse().getId(): " + schedule.getCourse().getId());            System.out.println("- schedule.getDate(): " + schedule.getDate());
            System.out.println("- schedule.getDate()类型: " + (schedule.getDate() != null ? schedule.getDate().getClass().getName() : "null"));
            System.out.println("- schedule.getDateOriginal(): " + schedule.getDateOriginal());
            System.out.println("- schedule.getDateOriginal()类型: " + (schedule.getDateOriginal() != null ? schedule.getDateOriginal().getClass().getName() : "null"));System.out.println("- schedule.getStartTime(): " + schedule.getStartTime());
            System.out.println("- schedule.isCompleted(): " + schedule.isCompleted());
            System.out.println("- schedule.getCreatedAt(): " + schedule.getCreatedAt());
            System.out.println("- schedule.getUpdatedAt(): " + schedule.getUpdatedAt());
            
            TrainingSchedule savedSchedule = trainingScheduleRepository.save(schedule);
            System.out.println("保存成功，ID: " + savedSchedule.getId());
              return convertToDto(savedSchedule);
        } catch (Exception e) {
            System.err.println("添加训练计划失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("添加训练计划失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<TrainingScheduleDto> addBatchScheduleItems(BatchTrainingScheduleRequest request) {
        try {
            System.out.println("接收到批量添加训练计划请求: " + request);
            
            if (request.getSchedules() == null || request.getSchedules().isEmpty()) {
                throw new RuntimeException("批量添加请求中没有训练计划项");
            }
            
            // 获取当前用户
            User currentUser;
            try {
                currentUser = securityUtil.getCurrentUser();
                System.out.println("当前用户ID: " + currentUser.getId());
            } catch (Exception e) {
                System.err.println("获取当前用户失败: " + e.getMessage());
                throw new RuntimeException("用户认证失败或会话已过期，请重新登录", e);
            }
            
            List<TrainingScheduleDto> results = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            // 处理每个训练计划项
            for (TrainingScheduleRequest scheduleRequest : request.getSchedules()) {
                try {
                    System.out.println("处理训练计划项: " + scheduleRequest);
                    
                    // 查找课程
                    Course course;
                    try {
                        Long courseId = scheduleRequest.getCourseId();
                        if (courseId == null) {
                            throw new RuntimeException("课程ID不能为空");
                        }
                        
                        course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new RuntimeException("课程不存在，ID: " + courseId));
                        System.out.println("找到课程: " + course.getTitle());
                    } catch (Exception e) {
                        System.err.println("查找课程失败: " + e.getMessage());
                        throw new RuntimeException("无法找到指定课程: " + e.getMessage(), e);
                    }
                    
                    // 解析日期和时间
                    LocalDate date;
                    LocalTime startTime;
                    try {
                        date = parseDate(scheduleRequest.getDate());
                        startTime = LocalTime.parse(scheduleRequest.getStartTime(), TIME_FORMATTER);
                        System.out.println("解析后的日期: " + date + ", 时间: " + startTime);
                    } catch (Exception e) {
                        System.err.println("解析日期或时间失败: " + e.getMessage());
                        throw new RuntimeException("日期或时间格式不正确", e);
                    }
                    
                    // 检查是否已存在相同的训练计划
                    List<TrainingSchedule> existingSchedules = trainingScheduleRepository
                            .findByUserAndDate(currentUser, date);
                    
                    boolean isDuplicate = existingSchedules.stream()
                            .anyMatch(s -> s.getCourse().getId().equals(course.getId()) && 
                                         s.getStartTime().equals(startTime));
                    
                    if (isDuplicate) {
                        System.out.println("跳过重复的训练计划: " + date + " " + startTime + " " + course.getTitle());
                        continue; // 跳过重复的计划
                    }
                    
                    // 创建训练计划对象
                    TrainingSchedule schedule = TrainingSchedule.builder()
                            .user(currentUser)
                            .course(course)
                            .date(date)
                            .dateOriginal(date)
                            .startTime(startTime)
                            .completed(false)
                            .createdAt(today)
                            .updatedAt(today)
                            .build();
                    
                    // 保存训练计划
                    TrainingSchedule savedSchedule = trainingScheduleRepository.save(schedule);
                    System.out.println("保存成功，ID: " + savedSchedule.getId());
                    
                    // 转换为DTO并添加到结果列表
                    results.add(convertToDto(savedSchedule));
                    
                } catch (Exception e) {
                    System.err.println("处理单个训练计划项失败: " + e.getMessage());
                    // 继续处理下一个项目，而不是中断整个批量操作
                    // 可以选择记录错误但继续处理其他项目
                }
            }
            
            System.out.println("批量添加完成，成功添加 " + results.size() + " 个训练计划");
            return results;
            
        } catch (Exception e) {
            System.err.println("批量添加训练计划失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("批量添加训练计划失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public TrainingScheduleDto updateScheduleItem(Long itemId, TrainingScheduleRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        
        TrainingSchedule schedule = trainingScheduleRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("训练计划项不存在"));
        
        // 确保只能修改自己的训练计划
        if (!schedule.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权修改此训练计划");
        }
        
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("课程不存在"));
            schedule.setCourse(course);
        }
          if (request.getDate() != null) {
            LocalDate parsedDate = LocalDate.parse(request.getDate(), DATE_FORMATTER);
            schedule.setDate(parsedDate);
            schedule.setDateOriginal(parsedDate); // 同时更新两个日期字段
        }
          if (request.getStartTime() != null) {
            schedule.setStartTime(LocalTime.parse(request.getStartTime(), TIME_FORMATTER));
        }
        
        // 更新 updatedAt 字段
        schedule.setUpdatedAt(LocalDate.now());
        
        TrainingSchedule updatedSchedule = trainingScheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
    }

    @Override
    @Transactional
    public void deleteScheduleItem(Long itemId) {
        User currentUser = securityUtil.getCurrentUser();
        
        TrainingSchedule schedule = trainingScheduleRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("训练计划项不存在"));
        
        // 确保只能删除自己的训练计划
        if (!schedule.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权删除此训练计划");
        }
        
        trainingScheduleRepository.delete(schedule);
    }

    @Override
    public Map<String, List<TrainingScheduleDto>> getScheduleRange(String startDate, String endDate) {
        User currentUser = securityUtil.getCurrentUser();
        
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
        
        List<TrainingSchedule> schedules = trainingScheduleRepository.findByUserAndDateBetween(currentUser, start, end);
        
        return schedules.stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getDate().format(DATE_FORMATTER),
                        Collectors.mapping(this::convertToDto, Collectors.toList())
                ));
    }

    @Override
    @Transactional
    public TrainingScheduleDto updateCompletionStatus(Long itemId, boolean completed) {
        User currentUser = securityUtil.getCurrentUser();
        
        TrainingSchedule schedule = trainingScheduleRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("训练计划项不存在"));
          // 确保只能更新自己的训练计划
        if (!schedule.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权更新此训练计划");
        }
        
        schedule.setCompleted(completed);
        // 更新 updatedAt 字段
        schedule.setUpdatedAt(LocalDate.now());
        
        TrainingSchedule updatedSchedule = trainingScheduleRepository.save(schedule);
        
        return convertToDto(updatedSchedule);
    }
    
    @Override
    public TrainingStatisticsResponse getTrainingStatistics() {
        User currentUser = securityUtil.getCurrentUser();
        LocalDate today = LocalDate.now();
        
        // 今日训练时长
        TrainingStatisticsResponse.DailyTrainingTime todayTraining = getDailyTrainingTime(today, currentUser);
        
        // 近7天每日训练时长
        List<TrainingStatisticsResponse.DailyTrainingTime> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            last7Days.add(getDailyTrainingTime(date, currentUser));
        }
        
        // 近12周每周训练时长
        List<TrainingStatisticsResponse.WeeklyTrainingTime> last12Weeks = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate weekStart = today.minusWeeks(i).with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
            LocalDate weekEnd = weekStart.plusDays(6);
            last12Weeks.add(getWeeklyTrainingTime(weekStart, weekEnd, currentUser));
        }
        
        // 近12个月每月训练时长
        List<TrainingStatisticsResponse.MonthlyTrainingTime> last12Months = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            last12Months.add(getMonthlyTrainingTime(monthStart, monthEnd, currentUser));
        }
        
        return TrainingStatisticsResponse.builder()
                .todayTraining(todayTraining)
                .last7Days(last7Days)
                .last12Weeks(last12Weeks)
                .last12Months(last12Months)
                .build();
    }
    
    @Override
    public Integer getDailyTrainingDuration(LocalDate date) {
        User currentUser = securityUtil.getCurrentUser();
        TrainingStatisticsResponse.DailyTrainingTime dailyTime = getDailyTrainingTime(date, currentUser);
        return dailyTime.getTotalMinutes();
    }
      private TrainingStatisticsResponse.DailyTrainingTime getDailyTrainingTime(LocalDate date, User user) {
        // 获取当日已完成的课程训练时长
        List<TrainingSchedule> completedSchedules = trainingScheduleRepository
                .findByUserAndDateAndCompleted(user, date, true);
        
        int courseMinutes = completedSchedules.stream()
                .mapToInt(schedule -> {
                    Course course = schedule.getCourse();
                    return course.getDuration() != null ? course.getDuration() : 0;
                })
                .sum();
        
        // 获取当日自定义训练时长（从CourseProgress中获取）
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        
        List<CourseProgress> dayProgress = courseProgressRepository
                .findByUserIdAndLastAccessedAtBetween(user.getId(), dayStart, dayEnd);
        
        int customTrainingMinutes = dayProgress.stream()
                .mapToInt(progress -> progress.getTotalTimeSpent() != null ? progress.getTotalTimeSpent() : 0)
                .sum();
        
        return TrainingStatisticsResponse.DailyTrainingTime.builder()
                .date(date)
                .courseMinutes(courseMinutes)
                .customTrainingMinutes(customTrainingMinutes)
                .totalMinutes(courseMinutes + customTrainingMinutes)
                .build();
    }
    
    private TrainingStatisticsResponse.WeeklyTrainingTime getWeeklyTrainingTime(LocalDate weekStart, LocalDate weekEnd, User user) {
        int courseMinutes = 0;
        int customTrainingMinutes = 0;
        
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            TrainingStatisticsResponse.DailyTrainingTime dailyTime = getDailyTrainingTime(date, user);
            courseMinutes += dailyTime.getCourseMinutes();
            customTrainingMinutes += dailyTime.getCustomTrainingMinutes();
        }
        
        String weekLabel = String.format("%d-W%02d", 
                weekStart.getYear(), 
                weekStart.get(WeekFields.of(Locale.getDefault()).weekOfYear()));
        
        return TrainingStatisticsResponse.WeeklyTrainingTime.builder()
                .week(weekLabel)
                .courseMinutes(courseMinutes)
                .customTrainingMinutes(customTrainingMinutes)
                .totalMinutes(courseMinutes + customTrainingMinutes)
                .build();
    }
    
    private TrainingStatisticsResponse.MonthlyTrainingTime getMonthlyTrainingTime(LocalDate monthStart, LocalDate monthEnd, User user) {
        int courseMinutes = 0;
        int customTrainingMinutes = 0;
        
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            TrainingStatisticsResponse.DailyTrainingTime dailyTime = getDailyTrainingTime(date, user);
            courseMinutes += dailyTime.getCourseMinutes();
            customTrainingMinutes += dailyTime.getCustomTrainingMinutes();
        }
        
        String monthLabel = String.format("%d-%02d", monthStart.getYear(), monthStart.getMonthValue());
        
        return TrainingStatisticsResponse.MonthlyTrainingTime.builder()
                .month(monthLabel)
                .courseMinutes(courseMinutes)
                .customTrainingMinutes(customTrainingMinutes)
                .totalMinutes(courseMinutes + customTrainingMinutes)
                .build();
    }

    private TrainingScheduleDto convertToDto(TrainingSchedule schedule) {
        return TrainingScheduleDto.builder()
                .id(schedule.getId())
                .courseId(schedule.getCourse().getId())
                .courseName(schedule.getCourse().getTitle())
                .date(schedule.getDate().format(DATE_FORMATTER))
                .startTime(schedule.getStartTime().format(TIME_FORMATTER))
                .completed(schedule.isCompleted())
                .build();
    }
}