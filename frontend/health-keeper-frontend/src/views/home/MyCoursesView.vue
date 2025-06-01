<template>
  <div class="my-courses">
    <div class="header">
      <h2>我的课程</h2>
      <el-button type="primary" @click="createCourse">创建新课程</el-button>
    </div>

    <div class="courses-list" v-loading="loading">
      <el-empty v-if="courses.length === 0" description="暂无课程"></el-empty>
      
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in courses" :key="course.id">
          <el-card class="course-card">
            <template #header>
              <div class="card-header">
                <span class="title">{{ course.title }}</span>
              </div>
            </template>
            <div class="course-info">
              <p><i class="el-icon-time"></i> 时长：{{ course.duration }}分钟</p>
              <p><i class="el-icon-user"></i> 目标人群：{{ course.targetAudience }}</p>
              <p class="description">{{ course.description }}</p>
            </div>
            <div class="course-actions">
              <el-button type="primary" @click="viewCourse(course.id)">查看详情</el-button>
              <el-button type="danger" @click="deleteCourse(course.id)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import CourseService from '../../api/course'

export default {
  name: 'MyCoursesView',
  setup() {
    const router = useRouter()
    const courses = ref([])
    const loading = ref(false)

    const loadCourses = async () => {
      loading.value = true
      try {
        const response = await CourseService.getMyCourses()
        courses.value = response.data
      } catch (error) {
        console.error('获取课程列表失败:', error)
        ElMessage.error('获取课程列表失败，请重试')
      } finally {
        loading.value = false
      }
    }

    const createCourse = () => {
      router.push('/home/upload-course')
    }

    const viewCourse = (id) => {
      router.push(`/home/course/${id}`)
    }

    const deleteCourse = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除这个课程吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await CourseService.deleteCourse(id)
        ElMessage.success('删除成功')
        loadCourses()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除课程失败:', error)
          ElMessage.error('删除课程失败，请重试')
        }
      }
    }

    onMounted(() => {
      loadCourses()
    })

    return {
      courses,
      loading,
      createCourse,
      viewCourse,
      deleteCourse
    }
  }
}
</script>

<style scoped>
.my-courses {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.courses-list {
  margin-top: 20px;
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
</style> 