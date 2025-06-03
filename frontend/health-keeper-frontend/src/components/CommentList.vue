<template>
  <div class="comment-list">

    <!-- 评论输入框 -->
    <div class="comment-input">
      <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          resize="none"
      />
      <div class="comment-buttons">
        <el-button type="primary" @click="submitComment">
          发表评论
        </el-button>
      </div>
    </div>
    <!-- 评论列表 -->
    <div v-for="comment in comments" :key="comment.id" class="comment-item">
      <div class="comment-main">
        <div class="comment-header">
          <span class="username">{{ comment.username }}</span>
          <span class="time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-actions">
          <!--el-button type="primary" link size="small" @click="showReplyInput(comment)">
            回复
          </el-button-->
          <!-- 添加删除按钮 -->
          <el-button
              v-if="comment.userId === currentUser.id"
              type="text"
              @click="confirmDeleteComment(comment.id)"
          >
            删除
          </el-button>
        </div>
        
        <!-- 回复列表 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <div class="reply-header">
              <span class="username">{{ reply.username }}</span>
              <span class="reply-to">回复</span>
              <span class="username">{{ reply.replyToUsername }}</span>
              <span class="time">{{ formatTime(reply.createAt) }}</span>
            </div>
            <div class="reply-content">{{ reply.content }}</div>
            <div class="reply-actions">
              <el-button type="primary" link size="small" @click="showReplyInput(comment, reply)">
                回复
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 回复输入框 -->
      <div v-if="activeReply === comment.id" class="reply-input">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="2"
          placeholder="写下你的回复..."
          resize="none"
        />
        <div class="reply-buttons">
          <el-button size="small" @click="cancelReply">取消</el-button>
          <el-button type="primary" size="small" @click="submitReply(comment)">
            回复
          </el-button>
        </div>
      </div>
    </div>
    

  </div>
</template>

<script>
import { ref } from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import MomentService from '@/api/moment'

export default {
  name: 'CommentList',
  props: {
    momentId: {
      type: String,
      required: true
    }
  },
  emits: ['update-comments'],
  setup(props, { emit }) {
    const comments = ref([])
    const commentContent = ref('')
    const replyContent = ref('')
    const activeReply = ref(null)
    const replyToComment = ref(null)
    const currentUser = JSON.parse(localStorage.getItem('user')) || {}

    // 获取评论列表
    const loadComments = async () => {
      try {
        const response = await MomentService.getComments(props.momentId);
        comments.value = response.data;
      } catch (error) {
        console.error('加载评论失败:', error);
        ElMessage.error('加载评论失败');
      }
    }

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return '时间未知';
      const date = new Date(time);
      return isNaN(date.getTime()) ? '时间未知' : date.toLocaleString();
    }
    
    // 显示回复输入框
    const showReplyInput = (comment, reply = null) => {
      activeReply.value = comment.id
      replyToComment.value = reply || comment
      replyContent.value = ''
    }
    
    // 取消回复
    const cancelReply = () => {
      activeReply.value = null
      replyToComment.value = null
      replyContent.value = ''
    }
    
    // 提交评论
    const submitComment = async () => {
      if (!commentContent.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }

      try {
        const commentData = {
          content: commentContent.value.trim(),
          parentId: null,
          momentId: props.momentId
        };
        
        await MomentService.addComment(props.momentId, commentData);
        commentContent.value = '';
        await loadComments();
        emit('update-comments');
        ElMessage.success('评论成功');
      } catch (error) {
        console.error('评论失败:', error);
        ElMessage.error('评论失败: ' + (error.response?.data?.message || '未知错误'));
      }
    }

    // 提交回复
    const submitReply = async () => {
      if (!replyContent.value.trim()) {
        ElMessage.warning('请输入回复内容')
        return
      }

      try {
        const replyData = {
          content: replyContent.value.trim(),
          parentId: replyToComment.value.id
        };
        
        await MomentService.addComment(props.momentId, replyData);
        await loadComments();
        emit('update-comments');
        cancelReply();
        ElMessage.success('回复成功');
      } catch (error) {
        console.error('回复失败:', error);
        ElMessage.error('回复失败: ' + (error.response?.data?.message || '未知错误'));
      }
    }

    const confirmDeleteComment = (commentId) => {
      ElMessageBox.confirm('确定删除此评论吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await MomentService.deleteComment(props.momentId, commentId);
          ElMessage.success('删除成功');
          await loadComments();
          emit('update-comments');
        } catch (error) {
          console.error('删除失败:', error);
          ElMessage.error('删除失败: ' + (error.response?.data?.message || '未知错误'));
        }
      }).catch(() => {});
    };
    
    // 初始加载评论
    loadComments()
    
    return {
      comments,
      commentContent,
      replyContent,
      activeReply,
      replyToComment,
      currentUser,
      formatTime,
      showReplyInput,
      cancelReply,
      submitComment,
      submitReply,
      confirmDeleteComment,
      loadComments
    }
  }
}
</script>

<style scoped>
.comment-list {
  margin-top: 1rem;
}

.comment-item {
  margin-bottom: 1rem;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.username {
  font-weight: bold;
  color: #409eff;
}

.time {
  color: #999;
  font-size: 0.9rem;
}

.comment-content {
  margin-bottom: 0.5rem;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.reply-list {
  margin-top: 1rem;
  padding-left: 1rem;
  border-left: 2px solid #eee;
}

.reply-item {
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  background: white;
  border-radius: 4px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.reply-to {
  color: #666;
}

.reply-content {
  margin-bottom: 0.5rem;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
}

.reply-input {
  margin-top: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 4px;
}

.reply-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.comment-input {
  margin-top: 2rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.comment-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}
</style> 