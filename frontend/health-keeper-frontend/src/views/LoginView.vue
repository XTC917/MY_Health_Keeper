<template>
  <div class="login-container">
    <div class="login-box">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            type="text"
            id="username"
            v-model="username"
            required
            placeholder="请输入用户名"
          />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="请输入密码"
          />
        </div>
        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="register-link">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </p>
      <p class="back-link">
        <router-link to="/home/courses">返回首页</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import AuthService from '../api/auth';
import { ElMessage } from 'element-plus';

export default {
  name: 'LoginView',
  data() {
    return {
      username: '',
      password: '',
      loading: false
    };
  },
  methods: {
    async handleLogin() {
      if (!this.username || !this.password) {
        ElMessage.warning('请输入用户名和密码');
        return;
      }

      this.loading = true;
      try {
        const response = await AuthService.login(this.username, this.password);
        console.log('登录响应:', response);

        //const token = response.token;
        const token = localStorage.getItem('token');

        if (response.token) {

          // 保存令牌到 localStorage
          localStorage.setItem('token', token);
          console.log('令牌已保存:', token); // 控制台验证


          ElMessage.success('登录成功');
          const redirect = this.$route.query.redirect || '/home/profile';
          this.$router.push(redirect);
        } else {
          throw new Error('登录失败：未获取到token');
        }
      } catch (error) {
        console.error('登录失败:', error);
        ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码');
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-box {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.login-button {
  width: 100%;
  padding: 0.75rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
}

.login-button:hover {
  background-color: #45a049;
}

.login-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.register-link, .back-link {
  text-align: center;
  margin-top: 1rem;
  color: #666;
}

.register-link a, .back-link a {
  color: #4CAF50;
  text-decoration: none;
}

.register-link a:hover, .back-link a:hover {
  text-decoration: underline;
}
</style> 