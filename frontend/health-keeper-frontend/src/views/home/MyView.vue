<template>
  <div class="my-container">
    <div class="user-info">
      <el-avatar :size="80" :src="userInfo.avatar" />
      <div class="info">
        <h2>{{ userInfo.username }}</h2>
        <p>{{ userInfo.email }}</p>
      </div>
    </div>

    <div class="section">
      <h3>我创建的课程</h3>
      <div class="course-list">
        <el-card v-for="course in myCreatedCourses" :key="course.id" class="course-card">
          <div class="course-info">
            <h4>{{ course.title }}</h4>
            <p class="description">{{ course.description }}</p>
            <div class="course-meta">
              <span>{{ course.duration }}分钟</span>
              <span>{{ course.target }}</span>
            </div>
            <div class="course-actions">
              <el-button type="primary" size="small" @click="viewCourse(course)">查看</el-button>
              <el-button type="danger" size="small" @click="deleteCourse(course)">删除</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <div class="section">
      <h3>我的收藏</h3>
      <div class="course-list">
        <el-card v-for="course in myFavorites" :key="course.id" class="course-card">
          <div class="course-info">
            <h4>{{ course.title }}</h4>
            <p class="description">{{ course.description }}</p>
            <div class="course-meta">
              <span>{{ course.duration }}分钟</span>
              <span>{{ course.target }}</span>
            </div>
            <div class="course-actions">
              <el-button type="primary" size="small" @click="viewCourse(course)">查看</el-button>
              <el-button type="danger" size="small" @click="removeFavorite(course)">取消收藏</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userInfo = ref({})
const myFavorites = ref([])
const myCreatedCourses = ref([])

onMounted(() => {
  // 获取用户信息
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  userInfo.value = user

  // 获取收藏的课程
  const favorites = JSON.parse(localStorage.getItem('favorites') || '[]')
  myFavorites.value = favorites

  // 获取用户创建的课程
  const createdCourses = JSON.parse(localStorage.getItem('myCreatedCourses') || '[]')
  myCreatedCourses.value = createdCourses
})

const viewCourse = (course) => {
  router.push(`/home/courses/${course.id}`)
}

const removeFavorite = (course) => {
  const favorites = JSON.parse(localStorage.getItem('favorites') || '[]')
  const newFavorites = favorites.filter(item => item.id !== course.id)
  localStorage.setItem('favorites', JSON.stringify(newFavorites))
  myFavorites.value = newFavorites
  ElMessage.success('已取消收藏')
}

const deleteCourse = (course) => {
  ElMessageBox.confirm(
    '确定要删除这个课程吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 从所有课程列表中删除
    const courses = JSON.parse(localStorage.getItem('courses') || '[]')
    const newCourses = courses.filter(item => item.id !== course.id)
    localStorage.setItem('courses', JSON.stringify(newCourses))

    // 从用户创建的课程列表中删除
    const createdCourses = JSON.parse(localStorage.getItem('myCreatedCourses') || '[]')
    const newCreatedCourses = createdCourses.filter(item => item.id !== course.id)
    localStorage.setItem('myCreatedCourses', JSON.stringify(newCreatedCourses))
    myCreatedCourses.value = newCreatedCourses

    // 从收藏列表中删除（如果存在）
    const favorites = JSON.parse(localStorage.getItem('favorites') || '[]')
    const newFavorites = favorites.filter(item => item.id !== course.id)
    localStorage.setItem('favorites', JSON.stringify(newFavorites))
    myFavorites.value = newFavorites

    ElMessage.success('课程已删除')
  }).catch(() => {})
}
</script>

<style scoped>
.my-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info {
  margin-left: 20px;
}

.info h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.info p {
  margin: 5px 0 0;
  color: #909399;
}

.section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.section h3 {
  margin: 0 0 20px;
  font-size: 18px;
  color: #303133;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.course-card {
  display: flex;
  flex-direction: column;
}

.course-info {
  padding: 15px;
}

.course-info h4 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #303133;
}

.description {
  margin: 0 0 10px;
  color: #606266;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  color: #909399;
  font-size: 14px;
}

.course-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style> 