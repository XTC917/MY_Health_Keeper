/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me generate the 
 * front-end ui of my hand-drawn design as shown 
 * in the figure" 
 * and directly copied the code from its response.
 */
<template>
  <div class="training-view">
    <div class="header">
      <h2>训练任务</h2>
    </div>

    <div class="content">
      <div class="training-layout">          <!-- 左侧训练计划列表 -->
        <div class="training-schedule">
          <div class="date-header">
            <h3>{{ formatDate(selectedDate) }} 训练计划</h3>
            <!-- 显示当日训练时长 -->
            <div class="daily-duration">
              今日训练时长: {{ todayDuration }} 分钟
            </div>
            <el-button type="primary" size="small" @click="openAddCourseDialog">添加课程</el-button>
          </div>
          
          <div class="schedule-list" v-if="dailySchedule.length > 0">
            <div v-for="(item, index) in dailySchedule" :key="index" class="schedule-item">
              <div class="course-info">
                <div class="course-time">{{ formatTime(item.startTime) }}</div>
                <div class="course-name" @click="goToCourseDetail(item.courseId)">
                  {{ item.courseName }}
                  <el-icon class="course-link-icon"><ArrowRight /></el-icon>
                </div>
              </div>
              <div class="course-actions">
                <el-checkbox v-model="item.completed" @change="updateCompletion(item)"></el-checkbox>
                <el-button 
                  type="danger" 
                  size="small" 
                  circle 
                  @click="deleteScheduleItem(item)"
                  class="delete-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="当日暂无训练计划" />
        </div>

        <!-- 右侧日历 -->
        <div class="calendar-container">
          <div class="calendar-header">
            <el-button type="text" @click="prevMonth">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span>{{ currentYear }}年 {{ currentMonth }}月</span>
            <el-button type="text" @click="nextMonth">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          
          <div class="calendar">
            <div class="weekdays">
              <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
            </div>
            
            <div class="days">
              <div 
                v-for="{ date, inCurrentMonth, hasSchedule } in calendarDays" 
                :key="date.getTime()" 
                class="day"
                :class="{ 
                  'not-current-month': !inCurrentMonth, 
                  'has-schedule': hasSchedule,
                  'selected': isSameDate(date, selectedDate)
                }"
                @click="selectDate(date)"
              >
                <div class="solar-date">{{ date.getDate() }}</div>
                <div class="lunar-date">{{ getLunarDate(date) }}</div>
              </div>        </div>
      </div>
      
      <!-- 训练统计图表部分 -->
      <div class="training-statistics">
        <h3>训练时长统计</h3>
        
        <!-- 周统计图表 -->
        <div class="chart-container">
          <h4>近12周训练时长</h4>
          <div ref="weeklyChart" class="chart"></div>
        </div>
        
        <!-- 月统计图表 -->
        <div class="chart-container">
          <h4>近12个月训练时长</h4>
          <div ref="monthlyChart" class="chart"></div>
        </div>
      </div>
    </div>
      </div>
    </div>    <!-- 添加课程对话框 -->
    <el-dialog
      v-model="addCourseDialogVisible"
      title="添加课程到训练计划"
      width="600px"
    >
      <div class="add-course-form">
        <el-form :model="newScheduleItem" label-width="120px">
          <el-form-item label="选择课程">
            <el-select v-model="newScheduleItem.courseId" placeholder="请选择课程" style="width: 100%">
              <el-option
                v-for="course in enrolledCourses"
                :key="course.id"
                :label="course.title"
                :value="course.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="开始时间">
            <el-time-picker v-model="newScheduleItem.startTime" format="HH:mm" placeholder="选择时间" style="width: 100%" />
          </el-form-item>
          
          <!-- 添加模式选择 -->
          <el-form-item label="添加模式">
            <el-radio-group v-model="newScheduleItem.addMode">
              <el-radio value="single">单次添加</el-radio>
              <el-radio value="weekly">按周重复</el-radio>
              <el-radio value="dateRange">日期范围</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 按周重复选项 -->
          <template v-if="newScheduleItem.addMode === 'weekly'">
            <el-form-item label="选择周几">
              <el-checkbox-group v-model="newScheduleItem.weekdays">
                <el-checkbox value="1">周一</el-checkbox>
                <el-checkbox value="2">周二</el-checkbox>
                <el-checkbox value="3">周三</el-checkbox>
                <el-checkbox value="4">周四</el-checkbox>
                <el-checkbox value="5">周五</el-checkbox>
                <el-checkbox value="6">周六</el-checkbox>
                <el-checkbox value="0">周日</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="newScheduleItem.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </template>
          
          <!-- 日期范围选项 -->
          <template v-if="newScheduleItem.addMode === 'dateRange'">
            <el-form-item label="选择日期">
              <el-date-picker
                v-model="newScheduleItem.selectedDates"
                type="dates"
                placeholder="选择多个日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </template>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addCourseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addCourseToSchedule">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { ArrowLeft, ArrowRight, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import CourseService from '../../api/course'

const router = useRouter()

// 日历相关数据
const currentDate = ref(new Date())
const selectedDate = ref(new Date())
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 获取当前年月
const currentYear = computed(() => currentDate.value.getFullYear())
const currentMonth = computed(() => currentDate.value.getMonth() + 1)

// 切换月份
const prevMonth = () => {
  const date = new Date(currentDate.value)
  date.setMonth(date.getMonth() - 1)
  currentDate.value = date
}

const nextMonth = () => {
  const date = new Date(currentDate.value)
  date.setMonth(date.getMonth() + 1)
  currentDate.value = date
}

// 生成日历数据
const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  // 当月第一天和最后一天
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  
  // 需要显示的上个月的天数
  const prevMonthDays = firstDay.getDay()
  
  // 当月的总天数
  const daysInMonth = lastDay.getDate()
  
  // 日历格子总数 (显示6周)
  const totalCells = 6 * 7
  
  const days = []
  
  // 填充上个月的日期
  const prevMonth = new Date(year, month, 0)
  const daysInPrevMonth = prevMonth.getDate()
  
  for (let i = 0; i < prevMonthDays; i++) {
    const date = new Date(year, month - 1, daysInPrevMonth - prevMonthDays + i + 1)
    days.push({
      date,
      inCurrentMonth: false,
      hasSchedule: checkHasSchedule(date)
    })
  }
  
  // 填充当月的日期
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(year, month, i)
    days.push({
      date,
      inCurrentMonth: true,
      hasSchedule: checkHasSchedule(date)
    })
  }
  
  // 填充下个月的日期
  const remaining = totalCells - days.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month + 1, i)
    days.push({
      date,
      inCurrentMonth: false,
      hasSchedule: checkHasSchedule(date)
    })
  }
  
  return days
})

// 检查某天是否有训练计划
const checkHasSchedule = (date) => {
  // 这里应该从后端获取数据，现在先模拟一些数据
  const key = formatDateKey(date)
  return scheduleData.value[key] && scheduleData.value[key].length > 0
}

// 选择日期
const selectDate = (date) => {
  selectedDate.value = date
  loadDailySchedule()
}

// 比较两个日期是否是同一天
const isSameDate = (date1, date2) => {
  return date1.getFullYear() === date2.getFullYear() &&
         date1.getMonth() === date2.getMonth() &&
         date1.getDate() === date2.getDate()
}

// 格式化日期显示
const formatDate = (date) => {
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

// 格式化日期作为键
const formatDateKey = (date) => {
  // 使用标准格式yyyy-MM-dd，确保月和日都是两位数
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return ''
  
  if (typeof time === 'string') {
    return time
  }
  
  const hours = time.getHours().toString().padStart(2, '0')
  const minutes = time.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 简化版农历日期计算，获取农历日期或节气
const getLunarDate = (date) => {
  // 节日数据（公历）
  const festivals = {
    '1-1': '元旦',
    '2-14': '情人节',
    '3-8': '妇女节',
    '5-1': '劳动节',
    '6-1': '儿童节',
    '10-1': '国庆节',
    '12-25': '圣诞节'
  }
  
  // 农历日期
  const lunarDays = [
    '初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
    '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
    '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十'
  ]
  
  const month = date.getMonth() + 1
  const day = date.getDate()
  const festivalKey = `${month}-${day}`
  
  // 如果是节日，优先显示节日
  if (festivals[festivalKey]) {
    return festivals[festivalKey]
  }
  
  // 简单模拟农历日期，仅用于演示
  const dayOfYear = Math.floor((date - new Date(date.getFullYear(), 0, 1)) / 86400000)
  return lunarDays[dayOfYear % 30]
}

// 训练计划相关数据
const enrolledCourses = ref([])
const dailySchedule = ref([])
const scheduleData = ref({}) // 存储所有训练计划数据 { '2023-5-1': [{...}] }

// 训练统计相关数据
const trainingStatistics = ref({
  todayTraining: { totalMinutes: 0 },
  last12Weeks: [],
  last12Months: []
})
const todayDuration = ref(0)
const weeklyChart = ref(null)
const monthlyChart = ref(null)
let weeklyChartInstance = null
let monthlyChartInstance = null

// 从localStorage加载训练统计数据
const loadTrainingDataFromStorage = () => {
  const storedData = localStorage.getItem('trainingStatistics')
  if (storedData) {
    trainingStatistics.value = JSON.parse(storedData)
    todayDuration.value = trainingStatistics.value.todayTraining.totalMinutes
  }
}

// 保存训练统计数据到localStorage
const saveTrainingDataToStorage = () => {
  localStorage.setItem('trainingStatistics', JSON.stringify(trainingStatistics.value))
}

// 初始化训练统计数据
const initTrainingStatistics = () => {
  const today = new Date()
  const last12Weeks = []
  const last12Months = []

  // 生成近12周的数据
  for (let i = 11; i >= 0; i--) {
    const weekStart = new Date(today)
    weekStart.setDate(today.getDate() - (today.getDay() + 7 * i))
    const weekEnd = new Date(weekStart)
    weekEnd.setDate(weekStart.getDate() + 6)
    
    last12Weeks.push({
      week: `第${12-i}周`,
      totalMinutes: 0
    })
  }

  // 生成近12个月的数据
  for (let i = 11; i >= 0; i--) {
    const month = new Date(today.getFullYear(), today.getMonth() - i, 1)
    last12Months.push({
      month: `${month.getFullYear()}-${String(month.getMonth() + 1).padStart(2, '0')}`,
      totalMinutes: 0
    })
  }

  trainingStatistics.value = {
    todayTraining: { totalMinutes: 0 },
    last12Weeks,
    last12Months
  }
}

// 更新训练时长统计
const updateTrainingStatistics = (item, isCompleted) => {
  const today = new Date()
  
  // 更新今日训练时长
  if (isCompleted) {
    trainingStatistics.value.todayTraining.totalMinutes += 30 // 假设每个训练项目30分钟
  } else {
    trainingStatistics.value.todayTraining.totalMinutes = Math.max(0, trainingStatistics.value.todayTraining.totalMinutes - 30)
  }
  todayDuration.value = trainingStatistics.value.todayTraining.totalMinutes

  // 更新周统计
  const weekIndex = Math.floor((today.getDay() + 6) / 7) // 获取当前是第几周
  if (weekIndex < 12) {
    if (isCompleted) {
      trainingStatistics.value.last12Weeks[weekIndex].totalMinutes += 30
    } else {
      trainingStatistics.value.last12Weeks[weekIndex].totalMinutes = Math.max(0, trainingStatistics.value.last12Weeks[weekIndex].totalMinutes - 30)
    }
  }

  // 更新月统计
  const monthIndex = today.getMonth()
  if (monthIndex < 12) {
    if (isCompleted) {
      trainingStatistics.value.last12Months[monthIndex].totalMinutes += 30
    } else {
      trainingStatistics.value.last12Months[monthIndex].totalMinutes = Math.max(0, trainingStatistics.value.last12Months[monthIndex].totalMinutes - 30)
    }
  }

  // 保存到localStorage
  saveTrainingDataToStorage()
  
  // 更新图表
  initCharts()
}

// 加载用户已加入的课程
const loadEnrolledCourses = async () => {
  try {
    // 调用API获取用户已加入的课程
    const response = await CourseService.getEnrolledCourses()
    enrolledCourses.value = response.data || []
  } catch (error) {
    console.error('Error loading enrolled courses:', error)
    // API调用失败，显示空数组而不是示范数据
    enrolledCourses.value = []
    ElMessage.error('加载课程失败，请刷新页面重试')
  }
}

// 加载训练计划数据
const loadScheduleData = () => {
  // 从localStorage加载数据
  const storedData = localStorage.getItem('scheduleData')
  if (storedData) {
    scheduleData.value = JSON.parse(storedData)
  }
  
  // 加载当天的训练计划
  loadDailySchedule()
}

// 加载当天的训练计划
const loadDailySchedule = () => {
  const key = formatDateKey(selectedDate.value)
  dailySchedule.value = scheduleData.value[key] || []
}

// 加载训练统计数据
const loadTrainingStatistics = () => {
  // 从localStorage加载数据
  loadTrainingDataFromStorage()
  
  // 如果没有数据，初始化新的统计数据
  if (!trainingStatistics.value.last12Weeks.length) {
    initTrainingStatistics()
  }
  
  // 创建图表
  nextTick(() => {
    initCharts()
  })
}

// 初始化图表
const initCharts = () => {
  if (!trainingStatistics.value) return
  
  // 初始化周统计图表
  if (weeklyChart.value) {
    weeklyChartInstance = echarts.init(weeklyChart.value)
    const weeklyOption = {
      title: {
        text: '近12周训练时长',
        textStyle: {
          fontSize: 14
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          let result = params[0].name + '<br/>'
          params.forEach(item => {
            result += item.seriesName + ': ' + item.value + '分钟<br/>'
          })
          return result
        }
      },
      legend: {
        data: ['训练时长']
      },
      xAxis: {
        type: 'category',
        data: trainingStatistics.value.last12Weeks.map(item => item.week)
      },
      yAxis: {
        type: 'value',
        name: '时长(分钟)'
      },
      series: [
        {
          name: '训练时长',
          type: 'bar',
          data: trainingStatistics.value.last12Weeks.map(item => item.totalMinutes),
          itemStyle: {
            color: '#409EFF'
          }
        }
      ]
    }
    weeklyChartInstance.setOption(weeklyOption)
  }
  
  // 初始化月统计图表
  if (monthlyChart.value) {
    monthlyChartInstance = echarts.init(monthlyChart.value)
    const monthlyOption = {
      title: {
        text: '近12个月训练时长',
        textStyle: {
          fontSize: 14
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          let result = params[0].name + '<br/>'
          params.forEach(item => {
            result += item.seriesName + ': ' + item.value + '分钟<br/>'
          })
          return result
        }
      },
      legend: {
        data: ['训练时长']
      },
      xAxis: {
        type: 'category',
        data: trainingStatistics.value.last12Months.map(item => item.month)
      },
      yAxis: {
        type: 'value',
        name: '时长(分钟)'
      },
      series: [
        {
          name: '训练时长',
          type: 'line',
          data: trainingStatistics.value.last12Months.map(item => item.totalMinutes),
          smooth: true,
          itemStyle: {
            color: '#409EFF'
          }
        }
      ]
    }
    monthlyChartInstance.setOption(monthlyOption)
  }
}

// 监听窗口大小变化，调整图表大小
window.addEventListener('resize', () => {
  if (weeklyChartInstance) {
    weeklyChartInstance.resize()
  }
  if (monthlyChartInstance) {
    monthlyChartInstance.resize()
  }
})

// 跳转到课程详情页
const goToCourseDetail = (courseId) => {
  router.push(`/home/course/${courseId}`)
}

// 更新完成状态
const updateCompletion = async (item) => {
  try {
    // 更新本地数据
    const key = formatDateKey(selectedDate.value)
    if (scheduleData.value[key]) {
      const index = scheduleData.value[key].findIndex(i => i.id === item.id)
      if (index !== -1) {
        scheduleData.value[key][index].completed = item.completed
      }
    }
    
    // 更新训练统计
    updateTrainingStatistics(item, item.completed)
    
    ElMessage.success(item.completed ? '已标记为完成' : '已标记为未完成')
  } catch (error) {
    console.error('Error updating completion status:', error)
    ElMessage.error('更新状态失败，请重试')
    
    // 恢复原来的状态
    item.completed = !item.completed
  }
}

// 添加课程相关
const addCourseDialogVisible = ref(false)
const newScheduleItem = ref({
  courseId: null,
  startTime: null,
  addMode: 'single', // 'single', 'weekly', 'dateRange'
  weekdays: [], // 选择的周几 ['1', '2', '3']等
  dateRange: null, // 日期范围 ['2025-06-01', '2025-06-30']
  selectedDates: [] // 选择的多个日期
})

// 打开添加课程对话框
const openAddCourseDialog = () => {
  newScheduleItem.value = {
    courseId: null,
    startTime: null,
    addMode: 'single',
    weekdays: [],
    dateRange: null,
    selectedDates: []
  }
  addCourseDialogVisible.value = true
}

// 添加课程到训练计划
const addCourseToSchedule = async () => {
  if (!newScheduleItem.value.courseId || !newScheduleItem.value.startTime) {
    ElMessage.warning('请选择课程和时间')
    return
  }
  
  try {
    // 找到课程名称
    const course = enrolledCourses.value.find(c => c.id === newScheduleItem.value.courseId)
    if (!course) {
      ElMessage.error('课程不存在')
      return
    }
    
    let scheduleItems = []
    
    if (newScheduleItem.value.addMode === 'single') {
      // 单次添加模式
      const newItem = {
        id: Date.now(), // 使用时间戳作为临时ID
        courseId: newScheduleItem.value.courseId,
        courseName: course.title,
        startTime: formatTime(newScheduleItem.value.startTime),
        completed: false
      }
      
      // 更新本地数据
      const key = formatDateKey(selectedDate.value)
      if (!scheduleData.value[key]) {
        scheduleData.value[key] = []
      }
      scheduleData.value[key].push(newItem)
      
      // 更新当天计划
      dailySchedule.value = scheduleData.value[key]
      
      ElMessage.success('课程已添加到训练计划')
      addCourseDialogVisible.value = false
      return
    } 
    
    // 批量添加模式
    if (newScheduleItem.value.addMode === 'weekly') {
      // 按周重复模式
      if (!newScheduleItem.value.weekdays.length || !newScheduleItem.value.dateRange) {
        ElMessage.warning('请选择周几和日期范围')
        return
      }
      
      scheduleItems = generateWeeklySchedule()
    } else if (newScheduleItem.value.addMode === 'dateRange') {
      // 日期范围模式
      if (!newScheduleItem.value.selectedDates.length) {
        ElMessage.warning('请选择日期')
        return
      }
      
      scheduleItems = newScheduleItem.value.selectedDates.map(date => ({
        courseId: newScheduleItem.value.courseId,
        date: date,
        startTime: formatTime(newScheduleItem.value.startTime)
      }))
    }
    
    if (scheduleItems.length === 0) {
      ElMessage.warning('没有生成任何训练计划')
      return
    }
    
    // 批量添加训练计划
    scheduleItems.forEach(item => {
      const newItem = {
        id: Date.now() + Math.random(), // 使用时间戳加随机数作为临时ID
        courseId: item.courseId,
        courseName: course.title,
        startTime: item.startTime,
        completed: false
      }
      
      const key = item.date
      if (!scheduleData.value[key]) {
        scheduleData.value[key] = []
      }
      scheduleData.value[key].push(newItem)
    })
    
    ElMessage.success(`成功添加 ${scheduleItems.length} 个训练计划`)
    addCourseDialogVisible.value = false
    
    // 刷新数据
    loadDailySchedule()
    
  } catch (error) {
    console.error('添加训练计划失败:', error)
    ElMessage.error('添加失败，请重试')
  }
}

// 生成按周重复的训练计划
const generateWeeklySchedule = () => {
  const scheduleItems = []
  const [startDate, endDate] = newScheduleItem.value.dateRange
  
  const start = new Date(startDate)
  const end = new Date(endDate)
  const current = new Date(start)
  
  while (current <= end) {
    const dayOfWeek = current.getDay() // 0=周日, 1=周一, ..., 6=周六
    
    if (newScheduleItem.value.weekdays.includes(String(dayOfWeek))) {
      scheduleItems.push({
        courseId: newScheduleItem.value.courseId,
        date: formatDateKey(current),
        startTime: formatTime(newScheduleItem.value.startTime)
      })
    }
    
    // 移动到下一天
    current.setDate(current.getDate() + 1)
  }
  
  return scheduleItems
}

// 保存训练计划数据
const saveScheduleData = () => {
  localStorage.setItem('scheduleData', JSON.stringify(scheduleData.value))
}

// 删除训练计划项
const deleteScheduleItem = (item) => {
  ElMessageBox.confirm(
    '确定要删除这个训练任务吗？',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const key = formatDateKey(selectedDate.value)
    if (scheduleData.value[key]) {
      // 从数组中删除该项
      const index = scheduleData.value[key].findIndex(i => i.id === item.id)
      if (index !== -1) {
        // 如果任务已完成，需要更新训练统计
        if (item.completed) {
          updateTrainingStatistics(item, false)
        }
        scheduleData.value[key].splice(index, 1)
        // 更新当天计划显示
        dailySchedule.value = scheduleData.value[key]
        // 保存到localStorage
        saveScheduleData()
        ElMessage.success('训练任务已删除')
      }
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 在组件卸载时保存数据
onUnmounted(() => {
  saveScheduleData()
  saveTrainingDataToStorage()
})

onMounted(() => {
  loadEnrolledCourses()
  loadScheduleData()
  loadTrainingStatistics()
})
</script>

<style scoped>
.training-view {
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.header h2 {
  margin: 0;
  font-size: 18px;
  text-align: center;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.training-layout {
  display: flex;
  height: 100%;
  gap: 20px;
}

/* 左侧训练计划部分 */
.training-schedule {
  flex: 3;
  border: 1px solid #eee;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.date-header {
  padding: 10px;
  background-color: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.date-header h3 {
  margin: 0;
  font-size: 16px;
}

.schedule-list {
  padding: 10px;
  overflow-y: auto;
  flex: 1;
}

.schedule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.course-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.course-time {
  font-weight: bold;
  min-width: 60px;
}

.course-name {
  font-size: 14px;
  color: #409eff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.course-name:hover {
  text-decoration: underline;
}

.course-link-icon {
  font-size: 12px;
}

/* 右侧日历部分 */
.calendar-container {
  flex: 4;
  border: 1px solid #eee;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.calendar-header {
  padding: 10px;
  background-color: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.calendar {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  border-bottom: 1px solid #eee;
  background-color: #f5f7fa;
}

.weekday {
  padding: 10px;
  font-weight: bold;
}

.days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
  flex: 1;
}

.day {
  padding: 5px;
  text-align: center;
  cursor: pointer;
  position: relative;
  border-bottom: 1px solid #f0f0f0;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 60px;
}

.solar-date {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 2px;
}

.lunar-date {
  font-size: 10px;
  color: #909399;
}

.day:hover {
  background-color: #f5f7fa;
}

.not-current-month {
  color: #b4bccc;
}

.has-schedule::after {
  content: '';
  position: absolute;
  bottom: 5px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #409eff;
}

.selected {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: bold;
}

.add-course-form {
  margin-top: 20px;
}

/* 训练统计样式 */
.training-statistics {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 4px;
  background-color: #fafafa;
}

.training-statistics h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  text-align: center;
  color: #333;
}

.chart-container {
  margin-bottom: 30px;
}

.chart-container h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
  text-align: center;
}

.chart {
  width: 100%;
  height: 300px;
}

/* 日训练时长显示样式 */
.daily-duration {
  font-size: 14px;
  color: #409eff;
  font-weight: bold;
}

.course-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.delete-btn {
  padding: 4px;
}

.delete-btn .el-icon {
  font-size: 14px;
}
</style>