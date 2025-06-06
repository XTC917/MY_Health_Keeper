<template>
  <div class="upload-container">
    <div class="header">
      <el-button type="text" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>上传课程</h2>
    </div>

    <div class="content">
      <el-form
        ref="formRef"
        :model="courseForm"
        :rules="rules"
        label-width="100px"
        class="upload-form"
      >
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程标题" />
        </el-form-item>

        <!-- 注释掉封面图片上传
        <el-form-item label="封面图片" prop="cover">
          <el-upload
            class="cover-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            :http-request="handleCoverUpload"
          >
            <img v-if="courseForm.cover" :src="courseForm.cover" class="cover" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        -->

        <el-form-item label="课程时长" prop="duration">
          <el-input-number v-model="courseForm.duration" :min="1" :max="300" />
          <span class="unit">分钟</span>
        </el-form-item>

        <el-form-item label="目标人群" prop="target">
          <el-input v-model="courseForm.target" placeholder="请输入目标人群" />
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>

        <el-form-item label="课程视频" prop="videoUrl">
          <el-upload
            class="video-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeVideoUpload"
            :http-request="handleVideoUpload"
          >
            <video v-if="courseForm.videoUrl" :src="courseForm.videoUrl" class="video" controls />
            <el-icon v-else class="video-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="课程分类" prop="categories">
          <el-select
            v-model="courseForm.categories"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入课程分类"
            style="width: 100%"
          >
            <el-option label="燃脂" value="燃脂" />
            <el-option label="瘦腿" value="瘦腿" />
            <el-option label="增肌" value="增肌" />
            <el-option label="瑜伽" value="瑜伽" />
            <el-option label="普拉提" value="普拉提" />
            <el-option label="有氧" value="有氧" />
            <el-option label="力量" value="力量" />
            <el-option label="拉伸" value="拉伸" />
            <el-option label="核心" value="核心" />
            <el-option label="拳击" value="拳击" />
            <el-option label="舞蹈" value="舞蹈" />
            <el-option label="康复" value="康复" />
          </el-select>
        </el-form-item>

        <el-form-item label="课程难度" prop="level">
          <el-select v-model="courseForm.level" placeholder="请选择课程难度">
            <el-option label="H1" value="H1" />
            <el-option label="H2" value="H2" />
            <el-option label="H3" value="H3" />
            <el-option label="H4" value="H4" />
            <el-option label="H5" value="H5" />
          </el-select>
        </el-form-item>

        <el-form-item label="课程价格" prop="price">
          <el-input-number v-model="courseForm.price" :min="0" :max="1000" />
          <span class="unit">元</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import CourseService from '../../api/course'
import api from '../../api/config'

const router = useRouter()
const formRef = ref(null)

const courseForm = reactive({
  title: '',
  // cover: '',
  duration: 30,
  videoUrl: '',
  target: '',
  description: '',
  categories: [],
  level: 'H1',
  price: 0
})

const rules = {
  title: [{ required: true, message: '请输入课程标题', trigger: 'blur' }],
  // cover: [{ required: true, message: '请上传封面图片', trigger: 'change' }],
  duration: [{ required: true, message: '请输入课程时长', trigger: 'blur' }],
  videoUrl: [{ required: true, message: '请上传课程视频', trigger: 'change' }],
  target: [{ required: true, message: '请输入目标人群', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }],
  categories: [{ required: true, message: '请至少选择一个课程分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择课程难度', trigger: 'change' }]
}

// 注释掉图片上传相关函数
/*
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  return true
}

const handleCoverUpload = (options) => {
  const file = options.file
  const reader = new FileReader()
  reader.onload = (e) => {
    courseForm.cover = e.target.result
  }
  reader.readAsDataURL(file)
}
*/

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 创建课程对象
    const courseRequest = {
      title: courseForm.title,
      duration: courseForm.duration,
      videoUrl: courseForm.videoUrl,
      targetAudience: courseForm.target,
      description: courseForm.description,
      category: courseForm.categories.join(','),
      level: courseForm.level,
      price: courseForm.price
    }
    
    // 发送到后端API
    const response = await CourseService.createCourse(courseRequest)
    
    if (response.data) {
      ElMessage.success('课程上传成功')
      router.push('/home/my-courses')
    }
  } catch (error) {
    console.error('Error submitting form:', error)
    ElMessage.error(error.response?.data || '提交失败，请检查表单')
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const goBack = () => {
  router.back()
}

const handleVideoUpload = async (options) => {
  const file = options.file
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data) {
      courseForm.videoUrl = response.data
      ElMessage.success('视频上传成功')
    }
  } catch (error) {
    console.error('Error uploading video:', error)
    ElMessage.error('视频上传失败')
  }
}

const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  if (!isVideo) {
    ElMessage.error('只能上传视频文件！')
    return false
  }
  return true
}
</script>

<style scoped>
.upload-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  margin-left: 20px;
  font-size: 24px;
  color: #303133;
}

.content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
}

.upload-form {
  max-width: 600px;
  margin: 0 auto;
}

.unit {
  margin-left: 10px;
  color: #909399;
}

/* 注释掉上传组件样式
.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.cover {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.video-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 360px;
  height: 200px;
}

.video-uploader:hover {
  border-color: #409eff;
}

.video-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 360px;
  height: 200px;
  line-height: 200px;
  text-align: center;
}

.video {
  width: 360px;
  height: 200px;
  display: block;
  object-fit: contain;
}
*/
</style>