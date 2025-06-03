<template>
  <div class="comment-list">
    <!-- 评论输入框 -->
    <div class="comment-input">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="2"
        placeholder="写下你的评论..."
        :maxlength="500"
        show-word-limit
      />
      <div class="comment-actions">
        <el-button type="primary" @click="submitComment" :loading="submitting">
          发表评论
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comments">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <!-- 主评论 -->
        <div class="comment-main">
          <el-avatar :size="40" :src="comment.userAvatar || defaultAvatar" />
          <div class="comment-content">
            <div class="comment-header">
              <span class="username">{{ comment.username }}</span>
              <span class="time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
            <div class="comment-actions">
              <el-button type="text" @click="showReplyInput(comment)">回复</el-button>
              <el-button 
                v-if="comment.userId === currentUserId" 
                type="text" 
                @click="deleteComment(comment.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 回复输入框 -->
        <div v-if="replyingTo === comment.id" class="reply-input">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="2"
            :placeholder="`回复 ${comment.username}...`"
            :maxlength="500"
            show-word-limit
          />
          <div class="reply-actions">
            <el-button size="small" @click="cancelReply">取消</el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="submitReply(comment)" 
              :loading="submitting"
            >
              回复
            </el-button>
          </div>
        </div>

        <!-- 回复列表 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <el-avatar :size="32" :src="reply.userAvatar || defaultAvatar" />
            <div class="reply-content">
              <div class="reply-header">
                <span class="username">{{ reply.username }}</span>
                <template v-if="reply.replyToUsername">
                  <span class="reply-to">回复</span>
                  <span class="username">@{{ reply.replyToUsername }}</span>
                </template>
                <span class="time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <div class="reply-text">{{ reply.content }}</div>
              <div class="reply-actions">
                <el-button type="text" @click="showReplyInput(comment, reply)">回复</el-button>
                <el-button 
                  v-if="reply.userId === currentUserId" 
                  type="text" 
                  @click="deleteComment(reply.id)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { useUserStore } from '@/stores/user'
import { createComment, getComments, deleteComment as deleteCommentApi } from '@/api/comment'

const props = defineProps({
  momentId: {
    type: Number,
    required: true
  }
})

const userStore = useUserStore()
const currentUserId = userStore.user?.id

const comments = ref([])
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref(null)
const replyingToUser = ref(null)
const submitting = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await getComments(props.momentId)
    comments.value = response.data
  } catch (error) {
    console.error('获取评论失败:', error)
    ElMessage.error('获取评论失败')
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createComment(props.momentId, {
      content: commentContent.value
    })
    commentContent.value = ''
    await fetchComments()
    ElMessage.success('评论成功')
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

// 显示回复输入框
const showReplyInput = (comment, replyTo = null) => {
  replyingTo.value = comment.id
  replyingToUser.value = replyTo ? replyTo.userId : comment.userId
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyingToUser.value = null
  replyContent.value = ''
}

// 提交回复
const submitReply = async (comment) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submitting.value = true
  try {
    await createComment(props.momentId, {
      content: replyContent.value,
      parentId: comment.id,
      replyToUserId: replyingToUser.value
    })
    replyContent.value = ''
    replyingTo.value = null
    replyingToUser.value = null
    await fetchComments()
    ElMessage.success('回复成功')
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

// 删除评论
const deleteComment = async (commentId) => {
  try {
    await deleteCommentApi(commentId)
    await fetchComments()
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  return formatDistanceToNow(new Date(time), { addSuffix: true, locale: zhCN })
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  padding: 20px;
}

.comment-input {
  margin-bottom: 20px;
}

.comment-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.comment-item {
  margin-bottom: 20px;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.username {
  font-weight: 500;
  color: #333;
}

.time {
  color: #999;
  font-size: 12px;
}

.comment-text {
  margin: 8px 0;
  line-height: 1.5;
}

.replies {
  margin-left: 52px;
  margin-top: 12px;
}

.reply-item {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.reply-content {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.reply-to {
  color: #999;
  font-size: 12px;
}

.reply-text {
  margin: 4px 0;
  line-height: 1.5;
}

.reply-input {
  margin-left: 52px;
  margin-top: 12px;
}

.reply-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.el-button--text) {
  padding: 0 4px;
  font-size: 12px;
}
</style> 