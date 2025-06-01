<template>
  <div class="health-data">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
    </div>
    
    <!-- 数据卡片区域 -->
    <div class="data-cards">
      <div class="data-card">
        <div class="card-header">
          <h3>身高</h3>
          <el-button type="primary" size="small" @click="showEditDialog('height')">记录新数据</el-button>
        </div>
        <div class="card-value">{{ latestData.height || '--' }} <span class="unit">cm</span></div>
        <div ref="heightChartRef" class="card-chart"></div>
      </div>

      <div class="data-card">
        <div class="card-header">
          <h3>体重</h3>
          <el-button type="primary" size="small" @click="showEditDialog('weight')">记录新数据</el-button>
        </div>
        <div class="card-value">{{ latestData.weight || '--' }} <span class="unit">kg</span></div>
        <div ref="weightChartRef" class="card-chart"></div>
      </div>

      <div class="data-card">
        <div class="card-header">
          <h3>BMI</h3>
          <span class="bmi-status" :class="bmiClass">{{ bmiStatus }}</span>
        </div>
        <div class="card-value" :class="bmiClass">{{ latestBmi }}</div>
        <div ref="bmiChartRef" class="card-chart"></div>
      </div>
    </div>

    <!-- 历史记录按钮 -->
    <div class="history-button">
      <el-button type="info" @click="showHistoryDialog">查看历史记录</el-button>
    </div>

    <!-- 编辑数据对话框 -->
    <el-dialog
      v-model="editDialog.visible"
      :title="editDialog.title"
      width="30%"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item :label="editDialog.label">
          <el-input-number 
            v-model="editForm.value"
            :min="editDialog.min"
            :max="editDialog.max"
            :step="1"
          />
          <span class="unit">{{ editDialog.unit }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveNewData">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog
      v-model="historyDialog.visible"
      title="历史记录"
      width="60%"
    >
      <el-table :data="healthHistory" style="width: 100%">
        <el-table-column prop="recordedAt" label="时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.recordedAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="height" label="身高 (cm)" width="120" />
        <el-table-column prop="weight" label="体重 (kg)" width="120" />
        <el-table-column prop="bmi" label="BMI" width="120">
          <template #default="scope">
            {{ scope.row.bmi ? scope.row.bmi.toFixed(1) : '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="deleteRecord(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import HealthDataService from '@/api/healthData'

const router = useRouter()
const healthHistory = ref([])
const heightChartRef = ref(null)
const weightChartRef = ref(null)
const bmiChartRef = ref(null)

// 编辑对话框状态
const editDialog = ref({
  visible: false,
  title: '',
  label: '',
  unit: '',
  min: 0,
  max: 0,
  type: ''
})

// 历史记录对话框状态
const historyDialog = ref({
  visible: false
})

// 编辑表单数据
const editForm = ref({
  value: 0
})

// 最新数据计算
const latestData = computed(() => {
  if (healthHistory.value.length === 0) return {}
  return healthHistory.value[0]
})

// BMI相关计算
const latestBmi = computed(() => {
  if (!latestData.value.bmi) return '--'
  return latestData.value.bmi.toFixed(1)
})

const bmiStatus = computed(() => {
  const bmi = latestData.value.bmi
  if (!bmi) return '--'
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '超重'
  return '肥胖'
})

const bmiClass = computed(() => {
  const bmi = latestData.value.bmi
  if (!bmi) return ''
  if (bmi < 18.5) return 'underweight'
  if (bmi < 24) return 'normal'
  if (bmi < 28) return 'overweight'
  return 'obese'
})

// 显示编辑对话框
const showEditDialog = (type) => {
  const config = {
    height: {
      title: '记录身高',
      label: '身高',
      unit: 'cm',
      min: 100,
      max: 250
    },
    weight: {
      title: '记录体重',
      label: '体重',
      unit: 'kg',
      min: 20,
      max: 200
    }
  }
  
  if (!config[type]) {
    console.error('Invalid type:', type)
    return
  }
  
  editDialog.value = {
    ...config[type],
    visible: true,
    type
  }
  
  editForm.value = {
    value: latestData.value[type] || config[type].min
  }
}

// 显示历史记录对话框
const showHistoryDialog = () => {
  historyDialog.value.visible = true
}

// 保存新数据
const saveNewData = async () => {
  if (!editForm.value || editForm.value.value === undefined) {
    ElMessage.error('请输入有效的数据')
    return
  }
  
  const newData = {
    height: latestData.value?.height,
    weight: latestData.value?.weight
  }
  
  if (editDialog.value.type === 'height') {
    newData.height = editForm.value.value
  } else if (editDialog.value.type === 'weight') {
    newData.weight = editForm.value.value
  }
  
  // 计算BMI
  if (newData.height && newData.weight) {
    const heightInMeters = newData.height / 100
    newData.bmi = newData.weight / (heightInMeters * heightInMeters)
  }
  
  try {
    const response = await HealthDataService.addHealthData(newData)
    if (response.data) {
      await loadHistory()
      editDialog.value.visible = false
      ElMessage.success('数据保存成功')
    }
  } catch (error) {
    console.error('Error saving health data:', error)
    ElMessage.error(error.response?.data?.message || '保存数据失败')
  }
}

// 删除记录
const deleteRecord = async (id) => {
  try {
    await HealthDataService.deleteHealthData(id)
    await loadHistory()
    ElMessage.success('记录删除成功')
  } catch (error) {
    console.error('Error deleting health data:', error)
    ElMessage.error(error.response?.data?.message || '删除记录失败')
  }
}

// 初始化图表
const initCharts = () => {
  const chartOptions = {
    height: {
      name: '身高',
      color: '#409EFF',
      unit: 'cm'
    },
    weight: {
      name: '体重',
      color: '#67C23A',
      unit: 'kg'
    },
    bmi: {
      name: 'BMI',
      color: '#E6A23C',
      unit: ''
    }
  }
  
  const initSingleChart = (ref, type) => {
    if (!ref) return
    const chart = echarts.init(ref)
    const data = healthHistory.value.map(h => ({
      value: type === 'bmi' ? h.bmi?.toFixed(1) : h[type],
      time: new Date(h.recordedAt).toLocaleDateString()
    }))
    
    chart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: data.map(d => d.time),
        axisLabel: {
          interval: 0,
          rotate: 45
        }
      },
      yAxis: {
        type: 'value',
        name: chartOptions[type].unit
      },
      series: [{
        data: data.map(d => d.value),
        type: 'line',
        smooth: true,
        itemStyle: {
          color: chartOptions[type].color
        }
      }]
    })
  }
  
  initSingleChart(heightChartRef.value, 'height')
  initSingleChart(weightChartRef.value, 'weight')
  initSingleChart(bmiChartRef.value, 'bmi')
}

// 加载数据
const loadHistory = async () => {
  try {
    const response = await HealthDataService.getHealthData()
    healthHistory.value = response.data
    initCharts()
  } catch (error) {
    console.error('Error loading health data:', error)
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  }
}

// 返回上一页
const goBack = () => {
  router.push('/home/profile')
}

onMounted(() => {
  loadHistory()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (heightChartRef.value) echarts.getInstanceByDom(heightChartRef.value)?.resize()
    if (weightChartRef.value) echarts.getInstanceByDom(weightChartRef.value)?.resize()
    if (bmiChartRef.value) echarts.getInstanceByDom(bmiChartRef.value)?.resize()
  })
})
</script>

<style scoped>
.health-data {
  padding: 1rem;
}

.data-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.data-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.card-value {
  font-size: 2rem;
  font-weight: bold;
  margin: 1rem 0;
  color: #333;
}

.card-value .unit {
  font-size: 1rem;
  color: #666;
  margin-left: 0.5rem;
}

.card-chart {
  height: 200px;
  margin-top: 1rem;
}

.bmi-status {
  font-size: 0.9rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.underweight {
  color: #ff9800;
}

.normal {
  color: #4caf50;
}

.overweight {
  color: #ff9800;
}

.obese {
  color: #f44336;
}

.history-button {
  text-align: center;
  margin-top: 2rem;
}

.unit {
  margin-left: 0.5rem;
  color: #666;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}

.back-button {
  margin-bottom: 1rem;
}
</style> 