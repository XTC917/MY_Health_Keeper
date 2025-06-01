<template>
  <div class="course-list">
    <el-row :gutter="20">
      <el-col :span="6" v-for="course in courses" :key="course.id">
        <el-card class="course-card">
          <img :src="course.cover" class="course-cover" />
          <div class="course-info">
            <h3>{{ course.title }}</h3>
            <p>{{ course.description }}</p>
            <div class="course-meta">
              <span>{{ course.duration }}分钟</span>
              <span>{{ course.level }}</span>
            </div>
            <el-button type="primary" @click="handleJoinCourse(course)">加入课程</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const courses = ref([])

// 模拟课程数据
const mockCourses = [
  {
    id: 1,
    title: '瑜伽入门',
    description: '适合初学者的瑜伽课程',
    cover: 'https://example.com/yoga.jpg',
    duration: 60,
    level: '初级'
  },
  // ... 更多课程数据
]

onMounted(() => {
  courses.value = mockCourses
})

const handleJoinCourse = (course) => {
  // 检查是否登录
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const myCourses = JSON.parse(localStorage.getItem('myCourses') || '[]')
    
    // 检查是否已经加入过该课程
    if (myCourses.some(c => c.id === course.id)) {
      ElMessage.warning('您已经加入过该课程')
      return
    }
    
    // 添加课程到我的课程列表
    myCourses.push({
      ...course,
      joinTime: new Date().toISOString()
    })
    localStorage.setItem('myCourses', JSON.stringify(myCourses))
    
    ElMessage.success('加入课程成功')
  } catch (error) {
    console.error('Error joining course:', error)
    ElMessage.error('加入课程失败')
  }
}
</script>

<style scoped>
.course-list {
  padding: 20px;
}

.course-card {
  margin-bottom: 20px;
}

.course-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.course-info {
  padding: 10px;
}

.course-info h3 {
  margin: 0 0 10px 0;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  margin: 10px 0;
}

.course-meta span {
  color: #666;
}
</style> 