<template>
  <div class="training-list">
    <h2>我的课程</h2>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="courses.length === 0" class="empty">
      <el-empty description="您还没有加入任何课程" />
    </div>
    
    <div v-else class="course-grid">
      <el-card v-for="course in courses" :key="course.id" class="course-card">
        <img :src="course.coverImage" class="course-image" />
        <div class="course-info">
          <h3>{{ course.title }}</h3>
          <p class="description">{{ course.description }}</p>
          <div class="course-meta">
            <span>难度: {{ course.level }}</span>
            <span>时长: {{ course.duration }}</span>
          </div>
          <div class="course-progress" v-if="course.progress">
            <el-progress 
              :percentage="course.progress.progress" 
              :status="course.progress.status === 'COMPLETED' ? 'success' : ''"
            />
            <span class="progress-text">
              {{ course.progress.status === 'COMPLETED' ? '已完成' : '学习中' }}
            </span>
          </div>
          <el-button type="primary" @click="goToCourse(course.id)">
            继续学习
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEnrolledCourses } from '@/api/course'
import { ElMessage } from 'element-plus'

const router = useRouter()
const courses = ref([])
const loading = ref(true)

// 获取已加入的课程
const fetchEnrolledCourses = async () => {
  try {
    loading.value = true
    const response = await getEnrolledCourses()
    courses.value = response.data
  } catch (error) {
    console.error('Error fetching enrolled courses:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 跳转到课程详情页
const goToCourse = (courseId) => {
  router.push(`/course/${courseId}`)
}

onMounted(() => {
  fetchEnrolledCourses()
})
</script>

<style scoped>
.training-list {
  padding: 20px;
}

.loading {
  margin: 20px 0;
}

.empty {
  margin: 40px 0;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.course-card {
  height: 100%;
}

.course-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.course-info {
  padding: 15px;
}

.course-info h3 {
  margin: 0 0 10px;
}

.description {
  color: #666;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  color: #999;
}

.course-progress {
  margin-bottom: 15px;
}

.progress-text {
  display: block;
  margin-top: 5px;
  text-align: right;
  color: #666;
}
</style> 