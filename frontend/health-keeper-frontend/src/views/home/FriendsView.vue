<template>
  <div class="friends-page">
    <!-- 顶部栏 -->
    <div class="header">
      <h2>好友圈</h2>
      <el-button type="primary" circle @click="goToPost">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <!-- 好友管理区域 -->
    <div class="friend-management">
      <div class="friend-actions">
        <el-input v-model="friendUsername" placeholder="输入用户名"></el-input>
        <el-button type="primary" @click="sendFriendRequest">添加好友</el-button>
        <el-button
            type="danger"
            @click="friendUsername ? confirmDeleteFriend(friendUsername) : null"
            :disabled="!friendUsername"
        >
          删除好友
        </el-button>
      </div>

      <!-- 好友请求通知 -->
      <el-popover placement="bottom" trigger="click">
        <template #reference>
          <el-badge :value="pendingRequests.length" class="request-badge">
            <el-button>好友请求</el-button>
          </el-badge>
        </template>
        <div v-for="req in pendingRequests" :key="req.id" class="request-item">
          <span>{{ req.senderUsername }} 请求添加好友</span>
          <el-button size="small" @click="handleRequest(req, 'accept')">接受</el-button>
          <el-button size="small" type="danger" @click="handleRequest(req, 'reject')">拒绝</el-button>
        </div>
      </el-popover>

      <!-- 修改后的好友列表模板 -->
      <div class="friend-list">
        <h3>我的好友</h3>
        <div v-for="friend in friends" :key="friend.id" class="friend-item">
          <img :src="friend.avatar" class="avatar-small">
          <span>{{ friend.username }}</span>
          <!-- 添加删除按钮 -->
          <el-button
              type="danger"
              size="small"
              @click="confirmDeleteFriend(friend.username)"
          >
            删除好友
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 动态列表 -->
    <div class="moments-list">
      <div v-if="moments.length === 0" class="empty-state">
        <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" alt="empty" class="empty-icon">
        <p>还没有任何动态</p>
      </div>
      
      <div v-else v-for="moment in moments" :key="moment.id" class="moment-card">
        <!-- 用户信息 -->
        <div class="moment-header">
          <div class="user-info">
            <!--img :src="moment.user.avatar || defaultAvatar" class="avatar"-->
            <span class="username">{{ moment.username }}</span>
          </div>
          <span class="time">发布于：{{ formatTime(moment.createdAt) }}</span>
          <!-- 添加删除按钮 -->
          <el-button
              v-if="moment.userId === currentUser.id"
              type="danger"
              size="small"
              @click="confirmDeleteMoment(moment.id)"
          >
            删除
          </el-button>
        </div>
        
        <!-- 动态内容 -->
        <!--div class="moment-content" @click="goToDetail(moment)"-->
        <div class="moment-content">
          <p>{{ moment.content }}</p>
        </div>
        
        <!-- 媒体内容 -->
        <div v-if="moment.media && moment.media.length > 0" class="media-content" @click="goToDetail(moment)">
          <div class="media-preview">
            <img
              v-if="moment.media[0].type === 'image'"
              :src="moment.media[0].url"
              :alt="'media-0'"
              class="media-item"
            >
            <video
              v-else
              :src="moment.media[0].url"
              class="media-item video"
            ></video>
            <div v-if="moment.media.length > 1" class="media-count">
              <el-icon><Picture /></el-icon>
              {{ moment.media.length }}
            </div>
          </div>
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
        
        <!-- 点赞和评论 -->
        <div class="moment-actions">
          <div class="action-buttons">
            <el-button
              type="primary"
              link
              :class="{ 'like-active': isLiked(moment) }"
              @click="toggleLike(moment)"
            >
              <el-icon><component :is="isLiked(moment) ? 'StarFilled' : 'Star'" /></el-icon>
              <span style="margin-left: 4px">{{ moment.likesCount }}</span>
            </el-button>
            <el-button type="primary" link @click="showComments(moment)">
              <el-icon><ChatDotRound /></el-icon>
              评论 {{ moment.commentCount }}
            </el-button>
          </div>
          
          <!-- 点赞列表 -->
          <div v-if="moment.likes.length > 0" class="likes-list">
            <el-icon><Heart /></el-icon>
            <span v-for="(like, index) in moment.likes" :key="like.id">
              {{ like.username }}{{ index < moment.likes.length - 1 ? '，' : '' }}
            </span>
          </div>
        </div>
        
        <!-- 评论列表 -->
        <comment-list
          v-if="moment.showComments"
          :moment-id="moment.id"
          @update-comments="loadMoments"
        />
      </div>
    </div>
  </div>
</template>

<script>
import {ref, onMounted, reactive} from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Star, StarFilled, ChatDotRound, Picture } from '@element-plus/icons-vue'
import CommentList from '@/components/CommentList.vue'
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

export default {
  name: 'FriendsView',
  components: {
    CommentList,
    Plus,
    Star,
    StarFilled,
    ChatDotRound,
    Picture
  },
  setup() {
    const router = useRouter()
    const moments = ref([])
    const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

    const friendUsername = ref('')
    const pendingRequests = ref([])
    const friends = ref([])
    const currentUser = reactive(JSON.parse(localStorage.getItem('user')));


    // 加载动态列表
    const loadMoments = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          ElMessage.error('请先登录');
          router.push('/login');
          return;
        }

        const response = await axios.get('/api/moments/friends', {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (response.data && Array.isArray(response.data)) {
          moments.value = response.data.map(moment => ({
            ...moment,
            likes: moment.likes || [],
            comments: moment.comments || [],
            courses: moment.courses || [],
            showComments: false
          }));
        } else {
          console.error('Invalid response format:', response.data);
          ElMessage.error('数据格式错误');
        }
      } catch (error) {
        console.error('加载动态失败:', error);
        if (error.response?.status === 401) {
          ElMessage.error('登录已过期，请重新登录');
          router.push('/login');
        } else {
          ElMessage.error('加载动态失败: ' + (error.response?.data?.message || '未知错误'));
        }
      }
    };
    
    // 格式化时间
    const formatTime = (time) => {
      if (!time) return '时间未知'
      const parsed = new Date(time);
      return isNaN(parsed.getTime()) ? '时间未知' : parsed.toLocaleString();
    }
    
    // 跳转到发布页面
    const goToPost = () => {
      if (!localStorage.getItem('token')) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      router.push('/home/post-moment')
    }
    
    // 检查是否已点赞
    const isLiked =  (moment) => {

      const user = JSON.parse(localStorage.getItem('user') || '{}')
      console.log("是否点赞:",moment.likes)

      return moment.likes?.some(like => String(like.id) === String(user.id))
    }
    
    // 点赞/取消点赞
    const toggleLike = async (moment) => {
      console.log("点击了点赞按钮，moment ID:", moment.id);

      const user = JSON.parse(localStorage.getItem('user') || '{}')
      const token = localStorage.getItem('token');
      const isUserLiked = moment.likes?.some(like => like.id === user.id);
      console.log("正在点赞，moment ID:", moment.id); // ✅调试

      try {
        if (isUserLiked) {
          // 取消点赞
          await axios.delete(`/api/moments/${moment.id}/like`, {
            headers: {
              Authorization: `Bearer ${token}`
            }
          });
        } else {
          // 点赞
          await axios.post(`/api/moments/${moment.id}/like`, {}, {
            headers: {
              Authorization: `Bearer ${token}`
            }
          });
        }

        await loadMoments(); // 重新加载动态
      } catch (error) {
        ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'));
      }

      //localStorage.setItem('moments', JSON.stringify(moments))
    }
    
    // 显示/隐藏评论
    const showComments = (moment) => {
      moment.showComments = !moment.showComments
    }
    
    // 跳转到详情页
    const goToDetail = (/*moment*/) => {
      //router.push(`/home/moment/${moment.id}`)
    }
    
    onMounted(() => {
      loadMoments()
    })


    // 加载好友数据
    const loadFriends = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        ElMessage.error('请先登录');
        router.push('/login');
        return;
      }

      try {
        const res = await axios.get('/api/friends', {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (res.data && Array.isArray(res.data)) {
          friends.value = res.data;
        } else {
          console.error('Invalid friends data format:', res.data);
          ElMessage.error('好友数据格式错误');
        }
      } catch (error) {
        console.error('加载好友列表失败:', error);
        if (error.response?.status === 401) {
          ElMessage.error('登录已过期，请重新登录');
          router.push('/login');
        } else {
          ElMessage.error('加载好友列表失败: ' + (error.response?.data?.message || '未知错误'));
        }
      }
    };

    // 加载待处理请求
    const loadRequests = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        ElMessage.error('请先登录');
        router.push('/login');
        return;
      }

      try {
        const res = await axios.get('/api/friends/requests', {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (res.data && Array.isArray(res.data)) {
          pendingRequests.value = res.data;
        } else {
          console.error('Invalid requests data format:', res.data);
          ElMessage.error('请求数据格式错误');
        }
      } catch (error) {
        console.error('加载好友请求失败:', error);
        if (error.response?.status === 401) {
          ElMessage.error('登录已过期，请重新登录');
          router.push('/login');
        } else {
          ElMessage.error('加载好友请求失败: ' + (error.response?.data?.message || '未知错误'));
        }
      }
    };

    // 发送好友请求
    const sendFriendRequest = async () => {
      const token = localStorage.getItem('token');
      try {
        await axios.post('/api/friends/requests',
            { username: friendUsername.value },
            {
              headers: {
                Authorization: `Bearer ${token}` // 添加头
              }
            }
        );
        ElMessage.success('请求已发送');
        friendUsername.value = '';
      }catch (error) {
        if (error.response?.status === 404) {
          ElMessage.error('用户不存在');
        } else if (error.response?.status === 400) {
          ElMessage.error(error.response.data || '请求无效');
        } else {
          ElMessage.error('发送请求失败');
        }
      }

    }

    // 处理请求
    const handleRequest = async (req, action) => {
      const token = localStorage.getItem('token');
      try {
        await axios.put(
            `/api/friends/requests/${req.id}`,
            { action: action.toUpperCase() },
            {
              headers: {
                Authorization: `Bearer ${token}` // 添加头
              }
            }
        );
        await loadRequests();
        await loadFriends();
        ElMessage.success(action === 'accept' ? '好友添加成功' : '请求已拒绝');
      }catch (error) {
        ElMessage.error('操作失败，请重试');
      }

    }

// 在setup()中添加方法
    const confirmDeleteFriend = async (username) => {
      try {
        await ElMessageBox.confirm(
            `确定要删除好友 ${username} 吗？此操作不可恢复！`,
            '删除确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
        );

        // 用户确认后执行删除
        await removeFriend(username);
      } catch (error) {
        // 用户取消操作
        ElMessage.info('已取消删除');
      }
    };

    // 修改removeFriend方法，接受参数
    const removeFriend = async (username) => {
      try {
        await axios.delete(
            `/api/friends/${encodeURIComponent(username)}`,
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
              }
            }
        );
        ElMessage.success('删除成功');
        await loadFriends(); // 重新加载好友列表
      } catch (error) {
        ElMessage.error(error.response?.data || '删除失败');
      }
    };

    // 删除动态方法
    const confirmDeleteMoment = (momentId) => {
      ElMessageBox.confirm('确定删除此动态吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await axios.delete(`/api/moments/${momentId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
          });
          ElMessage.success('删除成功');
          await loadMoments(); // 重新加载动态列表
        } catch (error) {
          ElMessage.error('删除失败');
        }
      }).catch(() => {});
    };

    // 在onMounted中加载数据
    onMounted(async () => {
      await loadMoments()
      await loadFriends()
      await loadRequests()
    })
    
    return {
      moments,
      defaultAvatar,
      formatTime,
      goToPost,
      isLiked,
      toggleLike,
      showComments,
      loadMoments,
      goToDetail,
      friendUsername,
      pendingRequests,
      friends,
      currentUser,
      sendFriendRequest,
      handleRequest,
      confirmDeleteFriend,
      removeFriend,
      confirmDeleteMoment
    }
  }
}
</script>

<style scoped>
.friends-page {
  padding: 1rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.empty-icon {
  margin-bottom: 1rem;
}

.moment-card {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.moment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.username {
  font-weight: bold;
}

.time {
  text-align: center;
  color: #999;
  font-size: 0.9rem;
}

.moment-content {
  margin-bottom: 1rem;
  cursor: pointer;
}

.media-content {
  margin-bottom: 1rem;
  cursor: pointer;
}

.media-preview {
  position: relative;
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
}

.media-item {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-item.video {
  object-fit: contain;
  background: #000;
}

.media-count {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.course-tags {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  padding: 0.5rem;
  background: #f9f9f9;
  border-radius: 4px;
}

.tag-label {
  color: #666;
}

.moment-actions {
  border-top: 1px solid #eee;
  padding-top: 1rem;
}

.moment-actions .likes-list {
  margin-top: 6px;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.moment-actions .likes-list el-icon {
  margin-right: 6px;
}

.action-buttons {
  display: flex;
  gap: 2rem;
  margin-bottom: 0.5rem;
}

.action-buttons .el-button {
  margin-right: 12px;
}

.action-buttons .like-active {
  color: #ff4757;
}

.likes-list {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin-top: 8px;
}

.likes-list .el-icon {
  color: #ff4757;
}

.comment-list {
  margin-top: 16px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
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

.friend-management {
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f5f7fa;
  border-radius: 8px;
}

.friend-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.request-badge {
  margin-right: 1rem;
}

.request-item {
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.friend-list {
  margin-top: 1rem;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 8px;
  gap: 8px;
  justify-content: space-between; /* 让内容两端对齐 */
}

.friend-item .el-button {
  margin-left: auto; /* 按钮靠右 */
}

.avatar-small {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}
</style> 