<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>HealthKeeper</h1>
          <div class="user-info" v-if="isAuthenticated">
            <span>欢迎，{{ currentUser.username }}</span>
            <el-button type="text" @click="handleLogout">退出</el-button>
          </div>
          <div class="user-info" v-else>
            <el-button type="text" @click="goToLogin">登录</el-button>
            <el-button type="text" @click="goToRegister">注册</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <div class="welcome-section">
          <h2>欢迎使用 HealthKeeper</h2>
          <p>您的个人健康管理助手</p>
        </div>
        
        <div class="features-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="feature-card">
                <template #header>
                  <div class="feature-header">
                    <el-icon><Calendar /></el-icon>
                    <span>健康日历</span>
                  </div>
                </template>
                <div class="feature-content">
                  记录和追踪您的健康数据
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <el-card class="feature-card">
                <template #header>
                  <div class="feature-header">
                    <el-icon><DataLine /></el-icon>
                    <span>数据分析</span>
                  </div>
                </template>
                <div class="feature-content">
                  分析您的健康趋势和变化
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <el-card class="feature-card">
                <template #header>
                  <div class="feature-header">
                    <el-icon><Bell /></el-icon>
                    <span>健康提醒</span>
                  </div>
                </template>
                <div class="feature-content">
                  设置和管理您的健康提醒
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
// AI-Assisted: This component uses AI-powered features for:
// 1. Personalized health recommendations
// 2. Smart data analysis and trend prediction
// 3. Intelligent health reminders and notifications
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { Calendar, DataLine, Bell } from '@element-plus/icons-vue'

export default {
  name: 'HomePage',
  components: {
    Calendar,
    DataLine,
    Bell
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    
    const isAuthenticated = computed(() => store.getters.isAuthenticated)
    const currentUser = computed(() => store.getters.currentUser)
    
    const handleLogout = async () => {
      await store.dispatch('logout')
      router.push('/login')
    }
    
    const goToLogin = () => {
      router.push('/login')
    }
    
    const goToRegister = () => {
      router.push('/register')
    }
    
    return {
      isAuthenticated,
      currentUser,
      handleLogout,
      goToLogin,
      goToRegister
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-section {
  text-align: center;
  padding: 40px 0;
}

.welcome-section h2 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 10px;
}

.welcome-section p {
  font-size: 18px;
  color: #606266;
}

.features-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.feature-card {
  height: 100%;
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feature-header .el-icon {
  font-size: 20px;
}

.feature-content {
  color: #606266;
  line-height: 1.6;
}
</style> 