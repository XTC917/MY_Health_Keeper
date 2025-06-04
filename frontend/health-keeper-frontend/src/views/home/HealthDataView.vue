<template>
  <div class="health-data">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
    </div>
    
    <!-- 数据卡片区域 -->
    <div class="data-cards" v-loading="loading">
      <div class="data-card">
        <div class="card-header">
          <h3>身高</h3>
          <el-button type="primary" size="small" @click="showEditDialog('height')">记录新数据</el-button>
        </div>
        <div class="card-value">{{ latestData.height !== null ? latestData.height : '--' }} <span class="unit">cm</span></div>
        <div ref="heightChartRef" class="card-chart"></div>
      </div>

      <div class="data-card">
        <div class="card-header">
          <h3>体重</h3>
          <el-button type="primary" size="small" @click="showEditDialog('weight')">记录新数据</el-button>
        </div>
        <div class="card-value">{{ latestData.weight !== null ? latestData.weight : '--' }} <span class="unit">kg</span></div>
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
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item :label="editDialog.label">
          <el-input-number
            v-model="editForm.value"
            :min="editDialog.min"
            :max="editDialog.max"
            :step="0.1"
            :precision="1"
            controls-position="right"
          />
          <span class="unit">{{ editDialog.unit }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveNewData" :loading="loading">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog
      v-model="historyDialog.visible"
      title="历史记录"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-table :data="healthHistory" style="width: 100%" v-loading="loading">
        <el-table-column prop="recordedAt" label="时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.recordedAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="height" label="身高" width="120">
          <template #default="scope">
            {{ scope.row.height !== null ? scope.row.height : '--' }} cm
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="体重" width="120">
          <template #default="scope">
            {{ scope.row.weight !== null ? scope.row.weight : '--' }} kg
          </template>
        </el-table-column>
        <el-table-column prop="bmi" label="BMI" width="120">
          <template #default="scope">
            {{ scope.row.bmi !== null ? scope.row.bmi.toFixed(1) : '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="deleteRecord(scope.row.id)"
              :loading="loading"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import HealthDataService from '@/api/healthData'

export default {
  name: 'HealthDataView',
  setup() {
    const router = useRouter()
    const healthHistory = ref([])
    const heightChartRef = ref(null)
    const weightChartRef = ref(null)
    const bmiChartRef = ref(null)
    const loading = ref(false)
    
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
      const defaultData = {
        height: null,
        weight: null,
        bmi: null
      }
      
      if (!healthHistory.value || !Array.isArray(healthHistory.value) || healthHistory.value.length === 0) {
        return defaultData
      }
      
      // 找到最新的身高数据
      const latestHeight = healthHistory.value.find(h => h.height !== null)
      // 找到最新的体重数据
      const latestWeight = healthHistory.value.find(h => h.weight !== null)
      // 找到最新的BMI数据
      const latestBmi = healthHistory.value.find(h => h.bmi !== null)
      
      return {
        height: latestHeight?.height ?? null,
        weight: latestWeight?.weight ?? null,
        bmi: latestBmi?.bmi ?? null
      }
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
      
      // 确保配置存在
      if (!config[type]) {
        console.error('Invalid type:', type)
        return
      }
      
      // 设置对话框配置
      editDialog.value = {
        ...config[type],
        visible: true,
        type
      }
      
      // 初始化表单值
      editForm.value = {
        value: latestData.value[type] !== null ? latestData.value[type] : config[type].min
      }
    }
    
    // 显示历史记录对话框
    const showHistoryDialog = () => {
      historyDialog.value.visible = true
    }
    
    // 加载数据
    const loadHistory = async () => {
      try {
        loading.value = true
        const response = await HealthDataService.getHealthData(0, 100)
        console.log('API Response:', response)
        
        // 处理响应数据
        if (response.data && response.data.content) {
          // 提取健康数据，忽略嵌套的用户信息
          healthHistory.value = response.data.content.map(item => ({
            id: item.id,
            height: item.height,
            weight: item.weight,
            bmi: item.bmi,
            recordedAt: item.recordedAt
          }))
        } else {
          console.error('Unexpected response format:', response.data)
          healthHistory.value = []
        }
        
        console.log('Processed health history:', healthHistory.value)
        initCharts()
      } catch (error) {
        console.error('加载数据失败:', error)
        ElMessage.error('加载数据失败，请重试')
        healthHistory.value = []
      } finally {
        loading.value = false
      }
    }
    
    // 保存新数据
    const saveNewData = async () => {
      if (!editForm.value || editForm.value.value === undefined) {
        ElMessage.error('请输入有效的数据')
        return
      }
      
      const newData = {
        height: null,
        weight: null,
        bmi: null
      }
      
      // 获取最新的身高和体重数据
      const latestHeight = healthHistory.value.find(h => h.height !== null)?.height
      const latestWeight = healthHistory.value.find(h => h.weight !== null)?.weight
      
      // 设置当前输入的数据
      if (editDialog.value.type === 'height') {
        newData.height = editForm.value.value
        newData.weight = latestWeight // 保留最新的体重数据
      } else if (editDialog.value.type === 'weight') {
        newData.weight = editForm.value.value
        newData.height = latestHeight // 保留最新的身高数据
      }
      
      // 计算BMI（只要有身高和体重数据就计算）
      if (newData.height && newData.weight) {
        const heightInMeters = newData.height / 100
        newData.bmi = newData.weight / (heightInMeters * heightInMeters)
      }
      
      try {
        loading.value = true
        console.log('Saving new data:', newData)
        const response = await HealthDataService.addHealthData(newData)
        console.log('Save response:', response)
        
        if (response.data) {
          // 重新加载数据
          await loadHistory()
          editDialog.value.visible = false
          ElMessage.success('数据保存成功')
        } else {
          throw new Error('保存失败：服务器返回数据为空')
        }
      } catch (error) {
        console.error('保存数据失败:', error)
        ElMessage.error(error.message || '保存数据失败，请重试')
      } finally {
        loading.value = false
      }
    }
    
    // 删除记录
    const deleteRecord = async (id) => {
      try {
        loading.value = true
        await HealthDataService.deleteHealthData(id)
        await loadHistory()
        ElMessage.success('记录删除成功')
      } catch (error) {
        console.error('删除记录失败:', error)
        ElMessage.error('删除记录失败，请重试')
      } finally {
        loading.value = false
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
        })).reverse() // 反转数组以按时间正序显示
        
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
    
    return {
      healthHistory,
      latestData,
      latestBmi,
      bmiStatus,
      bmiClass,
      editDialog,
      historyDialog,
      editForm,
      heightChartRef,
      weightChartRef,
      bmiChartRef,
      loading,
      showEditDialog,
      showHistoryDialog,
      saveNewData,
      deleteRecord,
      goBack
    }
  }
}
</script>

<style scoped>
.health-data {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 20px;
}

.data-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.data-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.data-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
}

.card-value .unit {
  font-size: 16px;
  color: #909399;
  margin-left: 4px;
}

.card-chart {
  height: 200px;
  margin-top: 15px;
}

.bmi-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.underweight {
  color: #E6A23C;
  background-color: #FDF6EC;
}

.normal {
  color: #67C23A;
  background-color: #F0F9EB;
}

.overweight {
  color: #E6A23C;
  background-color: #FDF6EC;
}

.obese {
  color: #F56C6C;
  background-color: #FEF0F0;
}

.history-button {
  text-align: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-input-number {
  width: 180px;
}

.unit {
  margin-left: 8px;
  color: #909399;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .data-cards {
    grid-template-columns: 1fr;
  }
  
  .card-chart {
    height: 150px;
  }
  
  .card-value {
    font-size: 28px;
  }
}
</style> 