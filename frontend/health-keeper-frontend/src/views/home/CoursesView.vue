<template>
  <div class="courses-view">
    <div class="header">
      <h2>课程学习</h2>
    </div>

    <div class="content" v-loading="loading">
      <div v-if="courses.length === 0" class="empty-state">
        <el-empty description="暂无课程">
          <el-button type="primary" @click="goToUpload">上传课程</el-button>
        </el-empty>
      </div>
      <div v-else class="course-list">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CourseService from '../../api/course'

const router = useRouter()
const courses = ref([])
const loading = ref(false)

// 加载课程列表
const loadCourses = async () => {
  loading.value = true;
  try {
    console.log('Loading all courses...');
    const response = await CourseService.getAllCourses();
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
</style> 