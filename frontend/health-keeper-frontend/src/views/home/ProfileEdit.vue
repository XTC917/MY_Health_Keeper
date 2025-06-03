<template>Add commentMore actions
    <div class="profile-container">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <h2>个人信息管理</h2>
          </div>
        </template>
  
        <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
  
  <!--        &lt;!&ndash; 用户名 &ndash;&gt;-->
  <!--        <el-form-item label="用户名" prop="username">-->
  <!--          <el-input v-model="form.username" placeholder="请输入用户名" />-->
  <!--          <el-button type="primary" @click="updateField('username')">提交</el-button>-->
  <!--        </el-form-item>-->
  
  <!--        &lt;!&ndash; 密码 &ndash;&gt;-->
  <!--        <el-form-item label="密码" prop="password">-->
  <!--          <el-input v-model="form.password" type="password" placeholder="请输入密码" />-->
  <!--          <el-button type="primary" @click="updateField('password')">提交</el-button>-->
  <!--        </el-form-item>-->
  
          <!-- 邮箱 -->
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
  
          <!-- 手机号 -->
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
  
          <!-- 地址 -->
          <el-form-item label="地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入地址" />
          </el-form-item>
  
          <!-- 性别 -->
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择性别">
              <el-option label="男" value="male" />
              <el-option label="女" value="female" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
  
          <!-- 头像上传 -->
          <el-form-item label="头像" prop="avatar">
            <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
            >
              <img v-if="form.avatar" :src="form.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
  
          <el-form-item>
            <el-button type="primary" @click="handleAllSubmit">保存所有修改</el-button>
          </el-form-item>
  
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue'
  import { ElMessage } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import UserService from '../../api/user' // 提交接口封装
  import api from '../../api/config' // 你可以根据你的项目路径修改
  import { onMounted } from 'vue'
  
  onMounted(async () => {
    try {
      const res = await UserService.getProfile()
      if (res.data) {
        Object.assign(form, res.data)  // 将获取的用户信息填入 form
      }
    } catch (error) {
      ElMessage.error('获取用户信息失败')
    }
  })
  
  
  const formRef = ref(null)
  
  const form = reactive({
    username: '',
    password: '',
    email: '',
    phone: '',
    address: '',
    gender: '',
    avatar: ''
  })
  
  const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
    ],
    address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
    gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
  }
  
  const updateField = async (field) => {
    if (!formRef.value) return
    try {
      await formRef.value.validateField(field)
  
      const payload = { [field]: form[field] }
      await UserService.updateField(payload)
  
    } catch (error) {
      ElMessage.error(error.message || '提交失败')
    }
  }
  
  const beforeAvatarUpload = (file) => {
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
      ElMessage.error('只能上传图片文件！')
      return false
    }
    return true
  }
  
  const handleAvatarUpload = async (options) => {
    const file = options.file
    const formData = new FormData()
    formData.append('file', file)
  
    try {
      const response = await api.post('/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
  
      if (response.data) {
        form.avatar = response.data
        ElMessage.success('头像上传成功')
        await updateField('avatar')
      }
    } catch (error) {
      ElMessage.error('头像上传失败')
    }
  }
  
  const handleAllSubmit = async () => {
    const fields = ['email', 'phone', 'address', 'gender'] // 根据你显示的字段
    let allSuccess = true;
    for (const field of fields) {
      try {
        await updateField(field)
      } catch (error) {
        allSuccess = false;
        console.error(`字段 ${field} 更新失败`, error)
      }
    }
    if (allSuccess) {
      ElMessage.success('全部信息更新成功');
    } else {
      ElMessage.error('部分信息更新失败，请检查日志');
    }
  }
  
  </script>
  
  <style scoped>
  .profile-container {
    max-width: 600px;
    margin: 0 auto;
    padding: 40px 20px;
  }
  
  .avatar-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    width: 128px;
    height: 128px;
    position: relative;
    overflow: hidden;
  }
  
  .avatar-uploader:hover {
    border-color: #409eff;
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 128px;
    height: 128px;
    line-height: 128px;
    text-align: center;
  }
  
  .avatar {
    width: 128px;
    height: 128px;
    display: block;
    object-fit: cover;
    border-radius: 6px;
  }
  </style>