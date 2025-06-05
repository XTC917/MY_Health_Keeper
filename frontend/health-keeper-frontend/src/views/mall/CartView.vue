<template>
  <div class="cart-container">
    <h1>购物车</h1>
    
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
    
    <div v-else-if="!cart || !cart.items || cart.items.length === 0" class="empty">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="$router.push('/home/mall')">去购物</el-button>
      </el-empty>
    </div>
    
    <div v-else class="cart-content">
      <div class="cart-header">
        <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
      </div>

      <div class="cart-items">
        <div v-for="item in cart.items" :key="item.id" class="cart-item">
          <el-checkbox v-model="item.selected" @change="handleItemSelect" />
            <div class="product-info">
            <img :src="item.productImage" :alt="item.productName" class="product-image">
            <div class="product-details">
              <h3>{{ item.productName }}</h3>
              <p class="price">¥{{ item.price }}</p>
            </div>
          </div>
          <div class="quantity-control">
            <el-button-group>
              <el-button @click="updateQuantity(item.id, item.quantity - 1)" :disabled="item.quantity <= 1">-</el-button>
              <el-button disabled>{{ item.quantity }}</el-button>
              <el-button @click="updateQuantity(item.id, item.quantity + 1)">+</el-button>
            </el-button-group>
          </div>
          <div class="subtotal">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>
          <el-button type="danger" @click="removeItem(item.id)" icon="Delete" circle />
        </div>
      </div>
      
      <div class="cart-summary">
        <div class="selected-info">
          已选择 {{ selectedCount }} 件商品
        </div>
        <div class="total">
          总计：<span class="amount">¥{{ selectedTotal.toFixed(2) }}</span>
        </div>
        <el-button 
          type="primary" 
          size="large" 
          @click="checkout"
          :disabled="selectedCount === 0"
        >
          结算
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { cartApi } from '@/api/cart';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

export default {
  name: 'CartView',
  setup() {
    const router = useRouter();
    const cart = ref(null);
    const loading = ref(false);
    const error = ref(null);
    const selectAll = ref(false);

    const selectedCount = computed(() => {
      if (!cart.value || !cart.value.items) return 0;
      return cart.value.items.filter(item => item.selected).length;
    });

    const selectedTotal = computed(() => {
      if (!cart.value || !cart.value.items) return 0;
      return cart.value.items
        .filter(item => item.selected)
        .reduce((sum, item) => sum + (item.price * item.quantity), 0);
    });

    const handleSelectAll = (val) => {
      if (cart.value && cart.value.items) {
        cart.value.items.forEach(item => {
          item.selected = val;
        });
      }
    };

    const handleItemSelect = () => {
      if (cart.value && cart.value.items) {
        selectAll.value = cart.value.items.every(item => item.selected);
      }
    };

    const fetchCart = async () => {
      try {
        loading.value = true;
        const response = await cartApi.getCart();
        cart.value = response.data;
        // 初始化选中状态
        if (cart.value && cart.value.items) {
          cart.value.items.forEach(item => {
            item.selected = false;
          });
        }
      } catch (err) {
        console.error('Error fetching cart:', err);
        error.value = '获取购物车信息失败';
      } finally {
        loading.value = false;
      }
    };

    const updateQuantity = async (itemId, quantity) => {
      try {
        const response = await cartApi.updateCartItemQuantity(itemId, quantity);
        cart.value = response.data;
      } catch (err) {
        console.error('Error updating quantity:', err);
        ElMessage.error('更新数量失败');
      }
    };

    const removeItem = async (itemId) => {
      try {
        const response = await cartApi.removeFromCart(itemId);
        cart.value = response.data;
        ElMessage.success('删除成功');
      } catch (err) {
        console.error('Error removing item:', err);
        ElMessage.error('删除失败');
      }
    };

    const checkout = async () => {
      try {
        loading.value = true;
        // 只将选中的商品数据存储到 sessionStorage
        const cartData = {
          items: cart.value.items
            .filter(item => item.selected)
            .map(item => ({
            productId: item.productId,
            quantity: item.quantity,
            productName: item.productName,
            price: item.price,
            imageUrl: item.productImage
          }))
        };
        sessionStorage.setItem('cartOrder', JSON.stringify(cartData));
        // 跳转到结算页面
        router.push('/home/checkout');
      } catch (err) {
        console.error('Error during checkout:', err);
        ElMessage.error('结算失败');
      } finally {
        loading.value = false;
      }
    };

    onMounted(async () => {
      await fetchCart();
    });

    return {
      cart,
      loading,
      error,
      selectAll,
      selectedCount,
      selectedTotal,
      handleSelectAll,
      handleItemSelect,
      fetchCart,
      updateQuantity,
      removeItem,
      checkout
    };
  }
};
</script>

<style scoped>
.cart-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading {
  margin-top: 20px;
}

.error {
  margin-top: 20px;
  text-align: center;
}

.empty {
  margin-top: 20px;
  text-align: center;
}

.cart-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.cart-header {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.cart-items {
  margin-bottom: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-details {
  flex: 1;
}

.product-details h3 {
  margin: 0 0 10px 0;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.subtotal {
  font-weight: bold;
  color: #f56c6c;
  min-width: 100px;
  text-align: right;
}

.cart-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.selected-info {
  color: #666;
}

.total {
  font-size: 1.2em;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 1.5em;
}
</style> 