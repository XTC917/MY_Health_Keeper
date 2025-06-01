<template>
  <nav class="bottom-nav">
    <router-link to="/home/courses" class="nav-item" active-class="active">
      <i class="icon">📚</i>
      <span>课程学习</span>
    </router-link>
    <router-link to="/home/training" class="nav-item" active-class="active">
      <i class="icon">🏋️</i>
      <span>训练任务</span>
    </router-link>
    <router-link to="/home/friends" class="nav-item" active-class="active">
      <i class="icon">👥</i>
      <span>好友圈</span>
    </router-link>
    <router-link to="/home/mall" class="nav-item" active-class="active">
      <i class="icon">🛍️</i>
      <span>商城</span>
    </router-link>
    <router-link to="/home/profile" class="nav-item" active-class="active">
      <i class="icon">👤</i>
      <span>我的</span>
    </router-link>
  </nav>
</template>

<script>
export default {
  name: 'BottomNav'
};
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  background: white;
  padding: 8px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #666;
  padding: 8px 0;
  font-size: 12px;
}

.nav-item.active {
  color: #4CAF50;
}

.icon {
  font-size: 24px;
  margin-bottom: 4px;
}
</style> 