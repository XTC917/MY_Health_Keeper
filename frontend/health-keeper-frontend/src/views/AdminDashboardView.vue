<template>
  <div class="admin-dashboard">
    <h1>管理员仪表盘</h1>
    <div class="dashboard-content">
      <div class="card">
        <h3>用户管理</h3>
        <button @click="navigateToUserManagement">管理用户</button>
      </div>
      <div class="card">
        <h3>系统设置</h3>
        <button @click="navigateToSettings">系统设置</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboardView',
  methods: {
    navigateToUserManagement() {
      this.$router.push('/admin/users');
    },
    navigateToSettings() {
      this.$router.push('/admin/settings');
    }
  }
};
</script>

<style scoped>
.admin-dashboard {
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

button {
  padding: 0.75rem 1.5rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

button:hover {
  background-color: #45a049;
}
</style> 