<template>
  <div class="course-detail">
    <!-- ... existing template code ... -->
    
    <div class="course-actions">
      <el-button 
        :type="course.isEnrolled ? 'danger' : 'primary'"
        @click="handleEnroll"
      >
        {{ course.isEnrolled ? '退出课程' : '加入课程' }}
      </el-button>
    </div>
    
    <!-- ... existing template code ... -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import CourseService from '@/api/course'
import { ElMessage } from 'element-plus'

const route = useRoute()
const course = ref({})

// 获取课程详情
const fetchCourseDetail = async () => {
  try {
    const response = await CourseService.getCourseById(route.params.id)
    course.value = response.data
  } catch (error) {
    console.error('Error fetching course detail:', error)
    ElMessage.error(error.response?.data?.message || '获取课程详情失败')
  }
}

// 处理加入/退出课程
const handleEnroll = async () => {
  try {
    const response = await CourseService.enrollCourse(course.value.id)
    if (response.data.success) {
      course.value.isEnrolled = response.data.enrolled
      course.value.totalStudents = response.data.totalStudents
      ElMessage.success(course.value.isEnrolled ? '成功加入课程' : '已取消加入课程')
    } else {
      ElMessage.error(response.data.message || '操作失败，请重试')
    }
  } catch (error) {
    console.error('Error enrolling course:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

onMounted(() => {
  fetchCourseDetail()
})
</script>

<style scoped>
.course-detail {
  padding: 20px;
}

.course-actions {
  margin: 20px 0;
  text-align: center;
}
</style> 