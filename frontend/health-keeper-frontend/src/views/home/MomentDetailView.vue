<template>
  <div class="moment-detail">
    <!-- 顶部导航 -->
    <div class="header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2>动态详情</h2>
      <div style="width: 24px"></div>
    </div>

    <div v-if="moment" class="detail-content">
      <!-- 用户信息 -->
      <div class="user-info">
        <img :src="defaultAvatar" alt="avatar" class="avatar">
        <div class="user-meta">
          <span class="username">{{ moment.username }}</span>
          <span class="time">{{ formatTime(moment.createdAt) }}</span>
        </div>
      </div>

      <!-- 动态内容 -->
      <div class="content-text">
        {{ moment.content }}
      </div>

      <!-- 图片轮播 -->
      <div v-if="moment.media && moment.media.length > 0" class="media-carousel">
        <el-carousel :interval="4000" type="card" height="300px">
          <el-carousel-item v-for="(media, index) in moment.media" :key="index">
            <img
              v-if="media.type === 'image'"
              :src="media.url"
              :alt="'media-' + index"
              class="carousel-image"
              @click="previewImage(media.url)"
            >
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 打卡课程 -->
      <div v-if="moment.courses && moment.courses.length > 0" class="course-tags">
        <span class="tag-label">打卡课程：</span>
        <el-tag
          v-for="course in moment.courses"
          :key="course.id"
          size="small"
          type="success"
        >
          {{ course.name }}
        </el-tag>
      </div>

      <!-- 点赞按钮 -->
      <div class="actions">
        <el-button
          type="primary"
          :class="{ 'like-active': isLiked }"
          @click="toggleLike"
        >
          <el-icon><Heart /></el-icon>
          {{ moment.likes.length }}
        </el-button>
      </div>

      <!-- 点赞列表 -->
      <div v-if="moment.likes.length > 0" class="likes-list">
        <el-icon><Heart /></el-icon>
        <span v-for="(like, index) in moment.likes" :key="like.id">
          {{ like.username }}{{ index < moment.likes.length - 1 ? '，' : '' }}
        </span>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <h3>评论 {{ moment.commentCount || 0 }}</h3>
        <div class="comment-input">
          <el-input
            v-model="newComment"
            placeholder="写下你的评论..."
            :rows="2"
            type="textarea"
          />
          <el-button type="primary" @click="submitComment" :disabled="!newComment.trim()">
            发送
          </el-button>
        </div>
        
        <div class="comments-list">
          <div v-if="!moment.comments || moment.comments.length === 0" class="no-comments">
            暂无评论，快来抢沙发吧！
          </div>
          <div
            v-else
            v-for="comment in moment.comments"
            :key="comment.id"
            class="comment-item"
          >
            <img :src="defaultAvatar" alt="avatar" class="comment-avatar">
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-username">{{ comment.username }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Heart } from '@element-plus/icons-vue'
import MomentService from '@/api/moment'

const route = useRoute()
const router = useRouter()
const moment = ref(null)
const newComment = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const loadMoment = async () => {
  try {
    const response = await MomentService.getMomentById(route.params.id);
    console.log('动态详情数据:', response.data);
    if (response.data) {
      moment.value = {
        ...response.data,
        likes: response.data.likes || [],
        comments: response.data.comments || [],
        courses: response.data.courses || []
      };
      console.log('处理后的动态数据:', moment.value);
    } else {
      ElMessage.error('动态不存在');
      router.push('/home/friends');
    }
  } catch (error) {
    console.error('加载动态失败:', error);
    ElMessage.error('加载动态失败');
    router.push('/home/friends');
  }
}

const formatTime = (time) => {
  if (!time) return '时间未知';
  const date = new Date(time);
  return isNaN(date.getTime()) ? '时间未知' : date.toLocaleString();
}

const goBack = () => {
  router.back()
}

const isLiked = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return moment.value?.likes?.some(like => String(like.id) === String(user.id))
})

const toggleLike = async () => {
  try {
    if (isLiked.value) {
      await MomentService.unlikeMoment(moment.value.id);
    } else {
      await MomentService.likeMoment(moment.value.id);
    }
    await loadMoment(); // 重新加载动态
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'));
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return;
  
  try {
    const commentData = {
      content: newComment.value.trim()
    };
    
    await MomentService.addComment(moment.value.id, commentData);
    await loadMoment(); // 重新加载动态
    newComment.value = '';
    ElMessage.success('评论成功');
  } catch (error) {
    console.error('评论提交失败:', error);
    ElMessage.error('评论发表失败，请稍后再试');
  }
}

const previewImage = (url) => {
  const imgViewer = document.createElement('div')
  imgViewer.className = 'image-viewer'
  
  const img = document.createElement('img')
  img.src = url
  img.alt = 'preview'
  
  imgViewer.appendChild(img)
  document.body.appendChild(imgViewer)
  
  imgViewer.addEventListener('click', () => {
    document.body.removeChild(imgViewer)
  })
}

onMounted(() => {
  loadMoment()
})
</script>

<style scoped>
.moment-detail {
  min-height: 100vh;
  background-color: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.header h2 {
  margin: 0;
  font-size: 18px;
}

.detail-content {
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
  font-size: 16px;
}

.time {
  color: #999;
  font-size: 14px;
  margin-top: 4px;
}

.content-text {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.media-carousel {
  margin-bottom: 20px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.course-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}

.actions {
  margin-bottom: 16px;
}

.likes-list {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.comments-section {
  margin-top: 24px;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.comment-input .el-button {
  align-self: flex-start;
}

.comments-list {
  margin-top: 16px;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 20px 0;
}

.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.comment-username {
  font-weight: bold;
  font-size: 14px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.image-viewer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  cursor: pointer;
}

.image-viewer img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

.actions .like-active {
  color: #ff4757;
}

.likes-list .el-icon {
  color: #ff4757;
}
</style> 