<template>
  <div class="profile-page">
    <div class="user-info">
      <img :src="userAvatar" alt="avatar" class="avatar">
      <h2>{{ user.username || '未登录' }}</h2>
    </div>
    
    <div class="menu-list" v-if="isLoggedIn">
      <el-card class="menu-card">
        <div class="menu-item" @click="goToMyCourses">
          <el-icon><VideoCamera /></el-icon>
          <span>我创建的课程</span>
        </div>
        <div class="menu-item" @click="goToMyProducts">
          <el-icon><Goods /></el-icon>
          <span>我上架的商品</span>
        </div>
        <div class="menu-item" @click="goToOrders">
          <el-icon><List /></el-icon>
          <span>我的订单</span>
        </div>
        <div class="menu-item" @click="goToHealthData">
          <el-icon><DataLine /></el-icon>
          <span>健康数据</span>
        </div>
      </el-card>
      
      <div class="logout-button">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    
    <div v-else class="login-prompt">
      <p>请先登录以查看更多信息</p>
      <el-button type="primary" @click="goToLogin">去登录</el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  VideoCamera, 
  Goods, 
  List, 
  DataLine 
} from '@element-plus/icons-vue'
import AuthService from '../../api/auth'

export default {
  name: 'ProfileView',
  components: {
    VideoCamera,
    Goods,
    List,
    DataLine
  },
  setup() {
    const router = useRouter()
    const user = ref(AuthService.getCurrentUser() || {})
    
    // 计算属性
    const isLoggedIn = computed(() => {
      const currentUser = AuthService.getCurrentUser()
      return !!(currentUser && currentUser.token)
    })
    
    const userAvatar = computed(() => {
      return user.value.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    })
    
    // 路由跳转方法
    const goToMyCourses = () => {
      router.push('/home/my-courses')
    }
    
    const goToMyProducts = () => {
      router.push('/home/my-products')
    }
    
    const goToOrders = () => {
      router.push('/home/my-orders')
    }
    
    const goToHealthData = () => {
      router.push('/home/health-data')
    }
    
    const goToLogin = () => {
      router.push('/login')
    }
    
    // 退出登录
    const handleLogout = () => {
      AuthService.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
    }
    
    return {
      user,
      isLoggedIn,
      userAvatar,
      goToMyCourses,
      goToMyProducts,
      goToOrders,
      goToHealthData,
      goToLogin,
      handleLogout
    }
  }
}
</script>

<style scoped>
.profile-page {
  padding: 1rem;
}

.user-info {
  text-align: center;
  margin-bottom: 2rem;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin-bottom: 1rem;
}

.menu-card {
  margin-bottom: 1rem;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-item .el-icon {
  font-size: 1.2rem;
  color: #409eff;
}

.logout-button {
  text-align: center;
  margin-top: 2rem;
}

.login-prompt {
  text-align: center;
  margin-top: 2rem;
}

.login-prompt p {
  margin-bottom: 1rem;
  color: #666;
}
</style> 