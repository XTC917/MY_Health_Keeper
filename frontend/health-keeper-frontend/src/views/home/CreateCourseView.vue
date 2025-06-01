<template>
  <div class="create-course">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>创建课程</h2>
        </div>
      </template>

      <el-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="100px">
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程标题"></el-input>
        </el-form-item>

        <el-form-item label="课程时长" prop="duration">
          <el-input-number v-model="courseForm.duration" :min="1" placeholder="请输入课程时长（分钟）"></el-input-number>
        </el-form-item>

        <el-form-item label="目标人群" prop="targetAudience">
          <el-input v-model="courseForm.targetAudience" placeholder="请输入目标人群"></el-input>
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <el-input type="textarea" v-model="courseForm.description" rows="4" placeholder="请输入课程描述"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">创建课程</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api/config'

export default {
  name: 'CreateCourseView',
  setup() {
    const router = useRouter()
    const courseFormRef = ref(null)
    const loading = ref(false)

    const courseForm = reactive({
      title: '',
      duration: 30,
      targetAudience: '',
      description: ''
    })

    const rules = {
      title: [
        { required: true, message: '请输入课程标题', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      duration: [
        { required: true, message: '请输入课程时长', trigger: 'blur' },
        { type: 'number', min: 1, message: '时长必须大于0', trigger: 'blur' }
      ],
      targetAudience: [
        { required: true, message: '请输入目标人群', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入课程描述', trigger: 'blur' },
        { min: 10, message: '描述至少10个字符', trigger: 'blur' }
      ]
    }

    const handleSubmit = async () => {
      if (!courseFormRef.value) return

      try {
        await courseFormRef.value.validate()
        loading.value = true

        const response = await api.post('/courses', courseForm)
        
        ElMessage.success('课程创建成功！')
        router.push('/home/my-courses')
      } catch (error) {
        console.error('创建课程失败:', error)
        ElMessage.error(error.response?.data?.message || '创建课程失败，请重试')
      } finally {
        loading.value = false
      }
    }

    const handleCancel = () => {
      router.back()
    }

    return {
      courseForm,
      courseFormRef,
      rules,
      loading,
      handleSubmit,
      handleCancel
    }
  }
}
</script>

<style scoped>
.create-course {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.form-card {
  margin-top: 20px;
}

.card-header {
  text-align: center;
}

.el-input-number {
  width: 100%;
}
</style> 