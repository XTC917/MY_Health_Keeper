<template>
  <div class="success-container">
    <el-card class="success-card">
      <template #header>
        <div class="card-header">
          <h2>注册成功</h2>
        </div>
      </template>
      
      <div class="success-content">
        <el-icon class="success-icon"><Check /></el-icon>
        <p>恭喜您，注册成功！</p>
        <p>请登录您的账号开始使用</p>
      </div>
      
      <div class="success-footer">
        <el-button type="primary" @click="goToLogin">立即登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { Check } from '@element-plus/icons-vue'

export default {
  name: 'RegisterSuccessPage',
  components: {
    Check
  },
  setup() {
    const router = useRouter()
    
    const goToLogin = () => {
      router.push('/login')
    }
    
    return {
      goToLogin
    }
  }
}
</script>

<style scoped>
.success-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.success-card {
  width: 400px;
}

.card-header {
  text-align: center;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-content p {
  margin: 10px 0;
  color: #606266;
}

.success-footer {
  text-align: center;
  margin-top: 20px;
}
</style> 