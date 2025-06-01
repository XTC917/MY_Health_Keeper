<template>
  <div class="cart-container">
    <h1>购物车</h1>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="error" class="error">
      <el-alert
        title="加载失败"
        type="error"
        :description="error"
        show-icon
      />
      <el-button type="primary" @click="fetchCart">重试</el-button>
    </div>
    
    <div v-else-if="!cart || !cart.items || cart.items.length === 0" class="empty">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="$router.push('/home/mall')">去购物</el-button>
      </el-empty>
    </div>
    
    <div v-else class="cart-content">
      <el-table :data="cart.items" style="width: 100%">
        <el-table-column label="商品" width="300">
          <template #default="{ row }">
            <div class="product-info">
              <img :src="row.productImage" :alt="row.productName" class="product-image">
              <span>{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        
        <el-table-column label="数量" width="200">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.quantity" 
              :min="1" 
              @change="(value) => updateQuantity(row.id, value)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            ¥{{ row.subtotal }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="danger" @click="removeItem(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="cart-summary">
        <div class="total">
          <span>总计：</span>
          <span class="amount">¥{{ cart.totalAmount }}</span>
        </div>
        <el-button type="primary" size="large" @click="checkout">结算</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { cartApi } from '@/api/cart';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

export default {
  name: 'CartView',
  setup() {
    const router = useRouter();
    const cart = ref(null);
    const loading = ref(true);
    const error = ref(null);

    const fetchCart = async () => {
      loading.value = true;
      error.value = null;
      try {
        console.log('Fetching cart...');
        const response = await cartApi.getCart();
        console.log('Cart response:', response);
        if (response.data && response.data.items) {
          cart.value = response.data;
          console.log('Cart data:', cart.value);
        } else {
          throw new Error('Invalid cart data format');
        }
      } catch (err) {
        console.error('Error fetching cart:', err);
        error.value = err.response?.data?.message || err.message || '获取购物车失败';
        ElMessage.error(error.value);
        cart.value = null;
      } finally {
        loading.value = false;
      }
    };

    const updateQuantity = async (itemId, quantity) => {
      try {
        if (quantity <= 0) {
          ElMessage.warning('数量必须大于0');
          await fetchCart();
          return;
        }
        const response = await cartApi.updateCartItemQuantity(itemId, quantity);
        if (response.data && response.data.items) {
          cart.value = response.data;
          ElMessage.success('更新成功');
        } else {
          throw new Error('Invalid cart data format');
        }
      } catch (err) {
        console.error('Error updating quantity:', err);
        ElMessage.error(err.response?.data?.message || err.message || '更新数量失败');
        await fetchCart();
      }
    };

    const removeItem = async (itemId) => {
      try {
        const response = await cartApi.removeFromCart(itemId);
        if (response.data && response.data.items) {
          cart.value = response.data;
          ElMessage.success('删除成功');
        } else {
          throw new Error('Invalid cart data format');
        }
      } catch (err) {
        console.error('Error removing item:', err);
        ElMessage.error(err.response?.data?.message || err.message || '删除失败');
        await fetchCart();
      }
    };

    const checkout = async () => {
      try {
        loading.value = true;
        if (!cart.value || !cart.value.items || cart.value.items.length === 0) {
          ElMessage.warning('购物车为空');
          return;
        }
        // 将购物车数据存储到 sessionStorage
        const cartData = {
          items: cart.value.items.map(item => ({
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
        ElMessage.error(err.response?.data?.message || err.message || '结算失败');
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

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-summary {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
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