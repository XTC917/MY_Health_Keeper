<template>
  <div class="checkout-container">
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="error" class="error">
      <el-alert
        :title="error"
        type="error"
        show-icon
      />
    </div>

    <div v-else class="checkout-content">
      <div class="cart-items">
        <h2>购物清单</h2>
        <div v-for="item in items" :key="item.productId" class="cart-item">
          <img :src="item.imageUrl" :alt="item.productName" class="item-image">
          <div class="item-info">
            <h3>{{ item.productName }}</h3>
            <p class="price">¥{{ item.price }}</p>
            <p class="quantity">数量：{{ item.quantity }}</p>
            <p class="subtotal">小计：¥{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>
      </div>

      <div class="payment-section">
        <h2>支付方式</h2>
        <el-radio-group v-model="paymentMethod">
          <el-radio label="alipay">支付宝</el-radio>
          <el-radio label="wechat">微信支付</el-radio>
        </el-radio-group>
      </div>

      <div class="order-summary">
        <h2>订单总计</h2>
        <div class="summary-item">
          <span>商品总额</span>
          <span>¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div class="summary-item">
          <span>运费</span>
          <span>¥0.00</span>
        </div>
        <div class="summary-item total">
          <span>实付金额</span>
          <span>¥{{ totalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <div class="actions">
        <el-button 
          type="primary" 
          size="large" 
          @click="confirmOrder"
          :loading="submitting"
        >
          确认支付
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { cartApi } from '@/api/cart';
import { orderApi } from '@/api/order';
import { ElMessage } from 'element-plus';

export default {
  name: 'CheckoutView',
  setup() {
    const router = useRouter();
    const items = ref([]);
    const totalAmount = ref(0);
    const shippingFee = ref(0);
    const paymentMethod = ref('alipay');
    const loading = ref(false);
    const error = ref(null);
    const submitting = ref(false);

    const confirmOrder = async () => {
      try {
        submitting.value = true;
        const orderData = {
          items: items.value.map(item => ({
            productId: item.productId,
            quantity: item.quantity
          }))
        };
        console.log('Creating order with data:', orderData);
        const response = await orderApi.createOrder(orderData);
        console.log('Order creation response:', response);
        
        // 模拟支付过程
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        // 更新订单状态为已支付
        const updateResponse = await orderApi.updateOrderStatus(response.data.id, 'PAID');
        console.log('Order status update response:', updateResponse);
        
        ElMessage.success('支付成功');
        router.push('/home/my-orders');
      } catch (err) {
        console.error('Error in payment process:', err);
        ElMessage.error(err.response?.data?.message || '支付失败');
      } finally {
        submitting.value = false;
      }
    };

    const goBack = () => {
      router.back();
    };

    onMounted(async () => {
      try {
        // 检查是否有直接购买的商品
        const directOrder = sessionStorage.getItem('directOrder');
        if (directOrder) {
          const orderData = JSON.parse(directOrder);
          items.value = orderData.items;
          totalAmount.value = items.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
          sessionStorage.removeItem('directOrder');
        } else {
          // 检查是否有购物车结算
          const cartOrder = sessionStorage.getItem('cartOrder');
          if (cartOrder) {
            const orderData = JSON.parse(cartOrder);
            items.value = orderData.items;
            totalAmount.value = items.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
            sessionStorage.removeItem('cartOrder');
          } else {
            // 从购物车获取商品
            const response = await cartApi.getCart();
            items.value = response.data.items;
            totalAmount.value = response.data.totalAmount;
          }
        }
      } catch (err) {
        console.error('Error fetching cart:', err);
        ElMessage.error('获取购物车信息失败');
      }
    });

    return {
      items,
      totalAmount,
      shippingFee,
      paymentMethod,
      loading,
      error,
      submitting,
      confirmOrder,
      goBack
    };
  }
};
</script>

<style scoped>
.checkout-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 20px;
}

.loading {
  padding: 20px;
}

.error {
  margin: 20px 0;
}

.checkout-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.cart-items {
  margin-bottom: 30px;
}

.cart-item {
  display: flex;
  gap: 20px;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.item-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
}

.item-info h3 {
  margin: 0 0 10px 0;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.quantity {
  color: #666;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.payment-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.order-summary {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-item.total {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.actions .el-button {
  width: 200px;
}
</style> 