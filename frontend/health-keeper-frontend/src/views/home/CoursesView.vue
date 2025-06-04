<template>
  <div class="courses-view">
    <div class="header">
      <h2>课程学习</h2>
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程名称或标签"
          class="search-input"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-group">
        <span class="filter-label">课程分类：</span>
        <el-select
          v-model="selectedCategories"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择课程分类"
          class="filter-select"
          @change="handleFilterChange"
        >
          <el-option label="燃脂" value="燃脂" />
          <el-option label="瘦腿" value="瘦腿" />
          <el-option label="增肌" value="增肌" />
          <el-option label="瑜伽" value="瑜伽" />
          <el-option label="普拉提" value="普拉提" />
          <el-option label="有氧" value="有氧" />
          <el-option label="力量" value="力量" />
          <el-option label="拉伸" value="拉伸" />
          <el-option label="核心" value="核心" />
          <el-option label="拳击" value="拳击" />
          <el-option label="舞蹈" value="舞蹈" />
          <el-option label="康复" value="康复" />
        </el-select>
      </div>
      <div class="filter-group">
        <span class="filter-label">难度级别：</span>
        <el-select
          v-model="selectedLevels"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择难度级别"
          class="filter-select"
          @change="handleFilterChange"
        >
          <el-option label="H1" value="H1" />
          <el-option label="H2" value="H2" />
          <el-option label="H3" value="H3" />
          <el-option label="H4" value="H4" />
          <el-option label="H5" value="H5" />
        </el-select>
      </div>
    </div>

    <div class="content" v-loading="loading">
      <div v-if="filteredCourses.length === 0" class="empty-state">
        <el-empty description="暂无符合条件的课程">
          <el-button type="primary" @click="goToUpload">上传课程</el-button>
        </el-empty>
      </div>
      <div v-else class="course-list">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in filteredCourses" :key="course.id">
            <el-card class="course-card">
              <template #header>
                <div class="card-header">
                  <span class="title">{{ course.title }}</span>
                </div>
              </template>
              <div class="course-info">
                <p><i class="el-icon-time"></i> 时长：{{ course.duration }}分钟</p>
                <p><i class="el-icon-star"></i> 难度：{{ course.level }}</p>
                <p class="description">{{ course.description }}</p>
                <div class="course-tags" v-if="course.category">
                  <el-tag 
                    v-for="tag in course.category.split(',')" 
                    :key="tag"
                    size="small" 
                    type="success"
                    class="category-tag"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              <div class="course-actions">
                <el-button 
                  type="primary" 
                  :class="{ 'is-joined': course.isEnrolled }"
                  @click.stop="enrollCourse(course.id)"
                >
                  {{ course.isEnrolled ? '已加入' : '加入课程' }}
                </el-button>
                <el-button @click.stop="goToCourseDetail(course.id)">查看详情</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import CourseService from '../../api/course'

const router = useRouter()
const courses = ref([])
const loading = ref(false)
const searchQuery = ref('')
const selectedCategories = ref([])
const selectedLevels = ref([])
let searchTimeout = null

// 过滤课程
const filteredCourses = computed(() => {
  return courses.value.filter(course => {
    // 搜索关键词过滤
    const matchesSearch = !searchQuery.value || 
      course.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      course.description.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      course.category.toLowerCase().includes(searchQuery.value.toLowerCase())

    // 分类过滤
    const matchesCategories = selectedCategories.value.length === 0 || 
      course.category.split(',').some(cat => selectedCategories.value.includes(cat))

    // 难度过滤
    const matchesLevels = selectedLevels.value.length === 0 || 
      selectedLevels.value.includes(course.level)

    return matchesSearch && matchesCategories && matchesLevels
  })
})

// 加载课程列表
const loadCourses = async (query = '') => {
  loading.value = true;
  try {
    console.log('Loading courses...');
    const response = await CourseService.getAllCourses(query, selectedCategories.value, selectedLevels.value);
    console.log('Courses response:', response);
    
    if (response.data && Array.isArray(response.data)) {
      courses.value = response.data;
      console.log('Loaded courses:', courses.value);
    } else {
      console.error('Invalid courses data format:', response.data);
      ElMessage.error('课程数据格式错误，请稍后重试');
      courses.value = [];
    }
  } catch (error) {
    console.error('Error loading courses:', error);
    const errorMessage = error.response?.data?.message || '加载课程失败，请检查网络连接或稍后重试';
    ElMessage.error(errorMessage);
    courses.value = [];
  } finally {
    loading.value = false;
  }
}

// 处理搜索
const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }
  searchTimeout = setTimeout(() => {
    loadCourses(searchQuery.value);
  }, 300);
}

// 处理筛选变化
const handleFilterChange = () => {
  loadCourses(searchQuery.value);
}

// 加入课程
const enrollCourse = async (courseId) => {
  if (!courseId) {
    ElMessage.error('课程ID无效');
    return;
  }

  // 检查是否登录
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  if (!user.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  try {
    loading.value = true;
    // 向后端发送加入/退出请求
    const response = await CourseService.enrollCourse(courseId);
    const { enrolled, totalStudents } = response.data;

    // 提示用户当前操作结果
    if (enrolled) {
      ElMessage.success('成功加入课程');
    } else {
      ElMessage.success('成功退出课程');
    }

    // 更新课程状态
    courses.value = courses.value.map(course => {
      if (course.id === courseId) {
        return {
          ...course,
          isEnrolled: enrolled,
          totalStudents: totalStudents
        };
      }
      return course;
    });
  } catch (error) {
    console.error('Error toggling enrollment:', error);
    const errorMessage = error.response?.data?.message || '操作失败，请稍后重试';
    ElMessage.error(errorMessage);
  } finally {
    loading.value = false;
  }
}

// 跳转到课程详情
const goToCourseDetail = (courseId) => {
  router.push(`/home/course/${courseId}`)
}

// 跳转到上传课程页面
const goToUpload = () => {
  // 检查是否登录
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  if (!user.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  router.push('/home/upload-course')
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.courses-view {
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
  font-size: 18px;
}

.search-bar {
  width: 300px;
}

.search-input {
  width: 100%;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-list {
  padding: 10px;
}

.course-card {
  margin-bottom: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-weight: bold;
  font-size: 16px;
}

.course-info {
  margin-bottom: 15px;
}

.course-info p {
  margin: 5px 0;
}

.description {
  margin-top: 10px;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.is-joined {
  background-color: #67c23a;
  border-color: #67c23a;
}

.course-tags {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.filter-section {
  padding: 16px 20px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.filter-select {
  width: 200px;
}

.category-tag {
  border-radius: 12px;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 12px;
  }

  .filter-group {
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }
}
</style> 