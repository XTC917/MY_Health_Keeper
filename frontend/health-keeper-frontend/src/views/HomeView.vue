<template>
  <div class="home-container">
    <div class="content">
      <router-view></router-view>
    </div>
    <BottomNav />
  </div>
</template>

<script>
import BottomNav from '../components/BottomNav.vue';

export default {
  name: 'HomeView',
  components: {
    BottomNav
  }
};
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.content {
  flex: 1;
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}
</style> 