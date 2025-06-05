<template>
  <div class="personalized-diet">
    <el-tabs v-model="activeTab" class="diet-tabs">
      <el-tab-pane label="食物热量查询" name="calorie">
        <div class="search-section">
          <el-input
            v-model="searchQuery"
            placeholder="输入食物名称（中文或英文）"
            class="search-input"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select v-model="selectedCategory" placeholder="选择食物类别" class="category-select">
            <el-option
              v-for="category in foodCategories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </div>

        <div class="search-results" v-if="searchResults.length > 0">
          <el-table :data="searchResults" style="width: 100%">
            <el-table-column prop="name" label="食物名称" />
            <el-table-column prop="calories" label="热量(千卡/100g)" />
            <el-table-column prop="protein" label="蛋白质(g)" />
            <el-table-column prop="fat" label="脂肪(g)" />
            <el-table-column prop="carbs" label="碳水化合物(g)" />
          </el-table>
        </div>
        
        <div v-else-if="searchQuery" class="no-results">
          未找到相关食物信息
        </div>
      </el-tab-pane>

      <el-tab-pane label="AI饮食计划" name="plan">
        <div class="plan-section">
          <el-form :model="dietPlanForm" label-width="120px">
            <el-form-item label="目标热量">
              <el-input-number 
                v-model="dietPlanForm.targetCalories" 
                :min="1000" 
                :max="5000"
                :step="100"
              />
              <span class="unit">千卡/天</span>
            </el-form-item>
            
            <el-form-item label="饮食偏好">
              <el-checkbox-group v-model="dietPlanForm.preferences">
                <el-checkbox label="高蛋白">高蛋白</el-checkbox>
                <el-checkbox label="低脂">低脂</el-checkbox>
                <el-checkbox label="低碳水">低碳水</el-checkbox>
                <el-checkbox label="素食">素食</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="generateDietPlan" :loading="generating">
                生成饮食计划
              </el-button>
            </el-form-item>
          </el-form>

          <div v-if="dietPlan" class="diet-plan-result">
            <h3>您的个性化饮食计划</h3>
            <div v-for="(meal, index) in dietPlan" :key="index" class="meal-section">
              <h4>{{ meal.name }}</h4>
              <el-table :data="meal.foods" style="width: 100%">
                <el-table-column prop="name" label="食物" />
                <el-table-column prop="amount" label="份量" />
                <el-table-column prop="calories" label="热量(千卡)" />
              </el-table>
              <div class="meal-total">
                总热量: {{ meal.totalCalories }} 千卡
              </div>
            </div>
            <div class="plan-summary">
              <h4>每日总计</h4>
              <p>总热量: {{ totalCalories }} 千卡</p>
              <p>蛋白质: {{ totalProtein }}g</p>
              <p>脂肪: {{ totalFat }}g</p>
              <p>碳水化合物: {{ totalCarbs }}g</p>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { DietService } from '@/api/diet'

const activeTab = ref('calorie')
const searchQuery = ref('')
const selectedCategory = ref('')
const searchResults = ref([])
const generating = ref(false)
const dietPlan = ref(null)

const foodCategories = [
  { label: '全部', value: '' },
  { label: '水果', value: 'fruit' },
  { label: '蔬菜', value: 'vegetable' },
  { label: '肉类', value: 'meat' },
  { label: '主食', value: 'staple' },
  { label: '饮品', value: 'beverage' },
  { label: '坚果', value: 'nut' }
]

const dietPlanForm = ref({
  targetCalories: 2000,
  preferences: []
})

// 计算属性
const totalCalories = computed(() => {
  if (!dietPlan.value || !Array.isArray(dietPlan.value)) return 0
  return dietPlan.value.reduce((sum, meal) => sum + (meal.totalCalories || 0), 0)
})

const totalProtein = computed(() => {
  if (!dietPlan.value || !Array.isArray(dietPlan.value)) return 0
  return dietPlan.value.reduce((sum, meal) => {
    if (!meal.foods || !Array.isArray(meal.foods)) return sum
    return sum + meal.foods.reduce((mealSum, food) => mealSum + (food.protein || 0), 0)
  }, 0)
})

const totalFat = computed(() => {
  if (!dietPlan.value || !Array.isArray(dietPlan.value)) return 0
  return dietPlan.value.reduce((sum, meal) => {
    if (!meal.foods || !Array.isArray(meal.foods)) return sum
    return sum + meal.foods.reduce((mealSum, food) => mealSum + (food.fat || 0), 0)
  }, 0)
})

const totalCarbs = computed(() => {
  if (!dietPlan.value || !Array.isArray(dietPlan.value)) return 0
  return dietPlan.value.reduce((sum, meal) => {
    if (!meal.foods || !Array.isArray(meal.foods)) return sum
    return sum + meal.foods.reduce((mealSum, food) => mealSum + (food.carbs || 0), 0)
  }, 0)
})

// 方法
const handleSearch = async () => {
  if (!searchQuery.value) {
    searchResults.value = []
    return
  }

  try {
    const response = await DietService.searchFood(searchQuery.value, selectedCategory.value)
    searchResults.value = response.data
  } catch (error) {
    console.error('搜索食物失败:', error)
    ElMessage.error('搜索食物失败：' + (error.response?.data?.message || '未知错误'))
  }
}

const generateDietPlan = async () => {
  if (!dietPlanForm.value.targetCalories) {
    ElMessage.warning('请输入目标热量')
    return
  }
  generating.value = true
  try {
    const response = await DietService.generateDietPlan(
      dietPlanForm.value.targetCalories,
      dietPlanForm.value.preferences
    )
    if (response.data && response.data.meals) {
      dietPlan.value = response.data.meals
    } else {
      ElMessage.error('返回数据格式不正确')
    }
  } catch (error) {
    console.error('生成饮食计划失败:', error)
    ElMessage.error('生成饮食计划失败：' + (error.response?.data?.message || '未知错误'))
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.personalized-diet {
  padding: 20px;
}

.search-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
}

.category-select {
  width: 200px;
}

.no-results {
  text-align: center;
  color: #909399;
  margin-top: 20px;
}

.plan-section {
  max-width: 800px;
  margin: 0 auto;
}

.unit {
  margin-left: 10px;
  color: #909399;
}

.diet-plan-result {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.meal-section {
  margin-bottom: 20px;
}

.meal-section h4 {
  margin-bottom: 10px;
  color: #409EFF;
}

.meal-total {
  text-align: right;
  margin-top: 10px;
  font-weight: bold;
}

.plan-summary {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #dcdfe6;
}

.plan-summary h4 {
  margin-bottom: 15px;
  color: #303133;
}

.plan-summary p {
  margin: 5px 0;
  color: #606266;
}
</style> 