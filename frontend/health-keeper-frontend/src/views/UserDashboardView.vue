<template>
  <div class="dashboard">
    <h1>用户仪表盘</h1>
    <div class="dashboard-content">
      <div class="card">
        <h3>欢迎回来，{{ username }}</h3>
        <p>这是您的个人仪表盘</p>
      </div>
      <div class="card">
        <h3>健康数据</h3>
        <p>您的健康数据将在这里显示</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserDashboardView',
  computed: {
    username() {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      return user.username || '用户';
    }
  }
};
</script>

<style scoped>
.dashboard {
  padding: 2rem;
}

.dashboard-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card h3 {
  margin-bottom: 1rem;
  color: #333;
}
</style> 