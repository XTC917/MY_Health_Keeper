<template>
  <div class="checkout-page">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
    </div>
    
    <div class="checkout-container">
      <h2>订单结算</h2>
      
      <div v-if="order" class="order-details">
        <div class="order-items">
          <h3>商品清单</h3>
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <img :src="item.image" :alt="item.name" class="item-image">
            <div class="item-info">
              <h4>{{ item.name }}</h4>
              <p>¥{{ item.price }} × {{ item.quantity }}</p>
            </div>
            <div class="item-total">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
          </div>
        </div>
        
        <div class="order-summary">
          <h3>订单信息</h3>
          <div class="summary-item">
            <span>商品总额</span>
            <span>¥{{ order.total.toFixed(2) }}</span>
          </div>
          <div class="summary-item">
            <span>运费</span>
            <span>¥0.00</span>
          </div>
          <div class="summary-item total">
            <span>实付金额</span>
            <span>¥{{ order.total.toFixed(2) }}</span>
          </div>
        </div>
        
        <div class="payment-methods">
          <h3>支付方式</h3>
          <el-radio-group v-model="paymentMethod">
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信支付</el-radio>
          </el-radio-group>
        </div>
        
        <div class="checkout-actions">
          <el-button type="primary" @click="confirmPayment" :loading="loading">
            确认支付
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'CheckoutView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const order = ref(null)
    const paymentMethod = ref('alipay')
    const loading = ref(false)
    
    // 返回上一页
    const goBack = () => {
      router.back()
    }
    
    // 加载订单信息
    const loadOrder = () => {
      const orderId = route.query.orderId
      const orders = JSON.parse(localStorage.getItem('orders') || '[]')
      order.value = orders.find(o => o.id === orderId)
      
      if (!order.value) {
        ElMessage.error('订单不存在')
        router.push('/home/market')
      }
    }
    
    // 确认支付
    const confirmPayment = () => {
      loading.value = true
      
      // 模拟支付过程
      setTimeout(() => {
        const orders = JSON.parse(localStorage.getItem('orders') || '[]')
        const index = orders.findIndex(o => o.id === order.value.id)
        if (index !== -1) {
          orders[index].status = '已支付'
          orders[index].paymentTime = new Date().toISOString()
          localStorage.setItem('orders', JSON.stringify(orders))
        }
        
        loading.value = false
        ElMessage.success('支付成功')
        router.push('/home/orders')
      }, 1500)
    }
    
    onMounted(() => {
      loadOrder()
    })
    
    return {
      order,
      paymentMethod,
      loading,
      goBack,
      confirmPayment
    }
  }
}
</script>

<style scoped>
.checkout-page {
  padding: 1rem;
}

.back-button {
  margin-bottom: 1rem;
}

.checkout-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.order-details {
  margin-top: 2rem;
}

.order-items {
  margin-bottom: 2rem;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-bottom: 1px solid #eee;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
}

.item-total {
  font-weight: bold;
  color: #ff4d4f;
}

.order-summary {
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.summary-item.total {
  font-weight: bold;
  font-size: 1.2rem;
  color: #ff4d4f;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.payment-methods {
  margin-bottom: 2rem;
}

.checkout-actions {
  display: flex;
  justify-content: flex-end;
}
</style> 