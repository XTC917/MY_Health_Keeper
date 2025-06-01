<template>
  <div class="post-moment">
    <div class="header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2>发布动态</h2>
      <el-button type="primary" :disabled="!content.trim()" @click="checkLoginAndPublish">发布</el-button>
    </div>

    <div class="content">
      <el-input
        v-model="content"
        type="textarea"
        :rows="6"
        placeholder="分享你的健身心得..."
        resize="none"
      />

      <div class="media-upload">
        <el-upload
          v-model:file-list="fileList"
          :auto-upload="false"
          list-type="picture-card"
          :on-change="handleFileChange"
          :limit="9"
          multiple
          accept="image/*,video/*"
        >
          <el-icon><Plus /></el-icon>
          <template #file="{ file }">
            <div class="upload-item">
              <img
                v-if="file.raw.type.startsWith('image/')"
                class="el-upload-list__item-thumbnail"
                :src="file.url"
                alt=""
              />
              <video
                v-else
                class="el-upload-list__item-thumbnail"
                :src="file.url"
                controls
              ></video>
              <span class="el-upload-list__item-actions">
                <span class="el-upload-list__item-delete" @click.stop="handleRemove(file)">
                  <el-icon><Delete /></el-icon>
                </span>
              </span>
            </div>
          </template>
        </el-upload>
      </div>

      <div class="course-select" v-if="myCourses.length > 0">
        <div class="section-title">
          <span>添加打卡课程</span>
          <el-button type="primary" link @click="showCourseDialog = true">
            <el-icon><Plus /></el-icon>添加
          </el-button>
        </div>
        
        <!-- 已选课程展示 -->
        <div v-if="selectedCourses.length > 0" class="selected-courses">
          <el-tag
            v-for="course in selectedCourses"
            :key="course.id"
            closable
            @close="removeCourse(course)"
            class="course-tag"
            type="success"
          >
            {{ course.title }}
          </el-tag>
        </div>
        <div v-else class="no-courses">
          <el-empty description="还没有选择打卡课程" />
        </div>
      </div>

      <!-- 课程选择对话框 -->
      <el-dialog
        v-model="showCourseDialog"
        title="选择打卡课程"
        width="80%"
      >
        <div class="course-list">
          <el-checkbox-group v-model="selectedCourseIds">
            <div v-for="course in myCourses" :key="course.id" class="course-item">
              <el-checkbox :label="course.id">
                <div class="course-info">
                  <img :src="course.cover" :alt="course.title" class="course-cover">
                  <div class="course-details">
                    <span class="course-name">{{ course.title }}</span>
                    <span class="course-duration">时长: {{ course.duration }}分钟</span>
                    <span class="course-target">适合: {{ course.target }}</span>
                  </div>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showCourseDialog = false">取消</el-button>
            <el-button type="primary" @click="confirmCourseSelection">
              确定
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import axios from "axios";

const router = useRouter()
const content = ref('')
const fileList = ref([])
const myCourses = ref([])
const showCourseDialog = ref(false)
const selectedCourseIds = ref([])

// 计算已选课程
const selectedCourses = computed(() => {
  return myCourses.value.filter(course => 
    selectedCourseIds.value.includes(course.id)
  )
})

// 加载我的课程
const loadMyCourses = () => {
  const coursesStr = localStorage.getItem('myCourses')
  if (coursesStr) {
    myCourses.value = JSON.parse(coursesStr)
  }
}

// 移除已选课程
const removeCourse = (course) => {
  selectedCourseIds.value = selectedCourseIds.value.filter(id => id !== course.id)
}

// 确认课程选择
const confirmCourseSelection = () => {
  showCourseDialog.value = false
}

const handleFileChange = (uploadFile) => {
  const isImage = uploadFile.raw.type.startsWith('image/')
  const isVideo = uploadFile.raw.type.startsWith('video/')
  
  if (!isImage && !isVideo) {
    ElMessage.error('只能上传图片或视频文件！')
    fileList.value = fileList.value.filter(file => file.uid !== uploadFile.uid)
    return
  }
  
  const reader = new FileReader()
  reader.readAsDataURL(uploadFile.raw)
  reader.onload = () => {
    uploadFile.url = reader.result
  }
}

const handleRemove = (file) => {
  fileList.value = fileList.value.filter(item => item.uid !== file.uid)
}

// 检查登录状态并发布
const checkLoginAndPublish = () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  publishMoment()
}

const publishMoment = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请输入动态内容');
    return;
  }

  // 创建 FormData 对象
  const formData = new FormData();
  formData.append("content", content.value);

  // 添加文件（字段名需与后端一致）
  fileList.value.forEach((file) => {
    formData.append("files", file.raw); // 注意：使用 file.raw（原始文件对象）
  });

  // 添加课程ID（以 JSON 字符串形式传递）
  if (selectedCourseIds.value.length > 0) {
    formData.append("courseIds", JSON.stringify(selectedCourseIds.value));
  }

  try {
    // 发送 POST 请求
    const response = await axios.post("/api/moments", formData, {
      headers: {
        //"Content-Type": "multipart/form-data", // 必须设置 multipart/form-data
        Authorization: `Bearer ${localStorage.getItem("token")}`, // 携带 Token
      },
    });
    console.log('发布响应：', response);
    ElMessage.success('发布成功');
    router.push('/home/friends');
  } catch (error) {
    // 处理错误
    ElMessage.error('发布失败: ' + (error.response?.data || '未知错误'));
  }
};

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadMyCourses()
})
</script>

<style scoped>
.post-moment {
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
  padding: 20px;
  overflow-y: auto;
}

.media-upload {
  margin-top: 20px;
}

.upload-item {
  position: relative;
  width: 100%;
  height: 100%;
}

.upload-item img,
.upload-item video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.course-select {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 16px;
  color: #606266;
}

.selected-courses {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-radius: 4px;
}

.no-courses {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  text-align: center;
}

.course-tag {
  display: flex;
  align-items: center;
  padding: 6px 10px;
}

.course-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.course-item {
  padding: 12px;
  border-bottom: 1px solid #eee;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 8px;
}

.course-item:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.course-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 8px;
}

.course-cover {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.course-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.course-duration, .course-target {
  font-size: 14px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
}
</style> 