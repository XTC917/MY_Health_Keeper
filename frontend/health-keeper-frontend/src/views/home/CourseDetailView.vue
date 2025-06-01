<template>
  <div class="course-detail">
    <div class="header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2>课程详情</h2>
    </div>

    <div class="content" v-loading="loading">
      <div v-if="course">
        <div class="course-video">
          <video 

            :src="course.videoUrl"

            controls 
            class="video-player"
            :poster="course.cover"
          ></video>
        </div>
        
        <div class="course-info">
          <h1 class="title">{{ course.title }}</h1>
          <div class="meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>创作者：{{ course.author }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Timer /></el-icon>
              <span>时长：{{ course.duration }}分钟</span>
            </div>
            <div class="meta-item">
              <el-icon><UserFilled /></el-icon>
              <span>适宜人群：{{ course.target }}</span>
            </div>
          </div>
          <div class="description">
            <h3>课程简介</h3>
            <p>{{ course.description }}</p>
          </div>
          <div class="actions">
            <el-button 
              type="primary" 
              size="large" 
              @click="handleAction('startTraining')"
              :disabled="!isLoggedIn || !course.isEnrolled"
            >
              开始训练
            </el-button>
            <el-button 
              type="success" 
              size="large" 
              @click="handleAction('enroll')"
              :disabled="!isLoggedIn"
              :class="{ 'is-joined': course.isEnrolled }"
            >
              <el-icon><Plus /></el-icon>
              {{ course.isEnrolled ? '退出课程' : '加入课程' }}
            </el-button>
            <el-button 
              type="danger" 
              size="large" 
              @click="handleAction('like')"
              :disabled="!isLoggedIn"
            >
              <el-icon><Star /></el-icon>
              {{ course.likes || 0 }} 点赞
            </el-button>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comments-section">
          <h3>评论区</h3>
          <div class="comment-input" v-if="isLoggedIn">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="2"
              placeholder="写下你的评论..."
            />
            <el-button type="primary" @click="submitComment">发表评论</el-button>
          </div>
          <div v-else class="login-prompt">
            <p>请登录后发表评论</p>
            <el-button type="primary" @click="goToLogin">去登录</el-button>
          </div>
          
          <div class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="32" :src="comment.userAvatar" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="username">{{ comment.username }}</span>

                  <span class="time">{{ formatTime(comment.createdAt) }}</span>

                </div>
                <p class="comment-text">{{ comment.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="!loading" class="empty-state">
        <el-empty description="课程不存在"></el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Star, User, Timer, UserFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CourseService from '../../api/course'

import CourseCommentService from '../../api/courseComment'


const route = useRoute()
const router = useRouter()
const course = ref(null)
const newComment = ref('')
const comments = ref([])
const loading = ref(false)

// 检查是否登录
const isLoggedIn = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return !!user.token
})

// 加载课程详情
const loadCourseDetail = async () => {
  loading.value = true
  try {
    const courseId = route.params.id
    const response = await CourseService.getCourseById(courseId)
    course.value = {
      id: response.data.id,
      title: response.data.title,
      duration: response.data.duration,
      target: response.data.targetAudience,
      description: response.data.description,
      videoUrl: response.data.videoUrl,
      cover: response.data.coverImage || response.data.thumbnail,
      author: response.data.author|| '未知',

      likes: response.data.likeCount || 0,
      isEnrolled: response.data.isEnrolled || false
    }
    console.log(course.value.videoUrl)

    
    if (!course.value) {
      ElMessage.error('课程不存在')
      router.push('/home/courses')
    }
  } catch (error) {
    console.error('Error loading course detail:', error)
    ElMessage.error('加载课程失败')
    router.push('/home/courses')
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  loading.value = true
  try {
    const courseId = route.params.id
    const response = await CourseCommentService.getCommentsByCourseId(courseId)
    comments.value = response || []

    if (!comments.value.length) {
      ElMessage.info('该课程暂时没有评论')
    }
  } catch (error) {
    console.error('Error loading comments:', error)
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}


// 处理操作（点赞、开始训练等）
const handleAction = (action) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  switch (action) {
    case 'like':
      handleLike()
      break
    case 'startTraining':
      startTraining()
      break
    case 'enroll':
      handleEnroll()
      break
  }
}

const handleLike = async () => {
  try {
    // 调用后端点赞接口（后端已实现切换逻辑）
    const courseId = route.params.id
    const response = await CourseService.toggleLike(courseId)

    const { liked, likeCount } = response.data

    // 提示用户点赞/取消点赞结果
    if (liked) {
      ElMessage.success('点赞成功')
    } else {
      ElMessage.success('已取消点赞')
    }

    // 更新课程状态（同步更新点赞状态与数量）
    course.value.likes = likeCount
    course.value.liked = liked

  } catch (error) {
    console.error('Error toggling like:', error)
    ElMessage.error(error.response?.data?.message || '点赞操作失败，请稍后重试')
  }
}

// 加入课程处理
const handleEnroll = async () => {
  try {
    await CourseService.enrollCourse(course.value.id)
    course.value.isEnrolled = !course.value.isEnrolled
    ElMessage.success(course.value.isEnrolled ? '成功加入课程' : '已取消加入课程')
  } catch (error) {
    console.error('Error enrolling course:', error)
    ElMessage.error(error.response?.data || '操作失败，请稍后重试')
  }
}


const submitComment = async () => {

  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  // 检查用户是否登录
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (!user.id) {
    ElMessage.warning('请先登录')
    return
  }

  const commentData = {
    courseId: course.value.id,
    content: newComment.value
  }

  try {
    // 调用 createComment 方法提交评论
    const savedComment = await CourseCommentService.createComment(commentData)

    // 更新评论列表
    comments.value.unshift({
      id: savedComment.id,
      courseId: savedComment.courseId,
      userId: savedComment.userId,
      username: savedComment.username || user.username, // 后端可能返回 username，若无则用本地
      userAvatar: savedComment.userAvatar || user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      content: savedComment.content,
      createTime: savedComment.createTime
    })

    newComment.value = ''
    ElMessage.success('评论发表成功')
  } catch (error) {
    console.error('评论提交失败:', error)
    ElMessage.error('评论发表失败，请稍后再试')
  }
}

// 开始训练
const startTraining = () => {
  ElMessage.success('开始训练')
}

// 格式化时间
const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadCourseDetail()
  loadComments()
})
</script>

<style scoped>
.course-detail {
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.header h2 {
  flex: 1;
  margin: 0;
  text-align: center;
  font-size: 18px;
}

.content {
  flex: 1;
  overflow-y: auto;
}

.course-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-info {
  padding: 20px;
}

.title {
  margin: 0 0 16px;
  font-size: 24px;
  color: #303133;
}

.meta {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  color: #606266;
}

.description {
  margin-bottom: 24px;
}

.description h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #303133;
}

.description p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

.actions {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.comments-section {
  padding: 20px;
  border-top: 1px solid #eee;
}

.comments-section h3 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
}

.comment-input {
  margin-bottom: 20px;
}

.login-prompt {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 12px;
}

.comment-text {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

.actions .el-button.is-joined {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.actions .el-button.is-joined:hover {
  background-color: #f78989;
  border-color: #f78989;
}
</style> 