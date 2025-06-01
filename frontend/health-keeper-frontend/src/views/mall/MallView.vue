<template>
  <div class="mall-container">
    <div class="mall-header">
      <h1>健康商城</h1>
      <router-link to="/home/cart" class="cart-icon">
        <i class="icon">🛒</i>
        <span class="cart-count" v-if="cartItemCount > 0">{{ cartItemCount }}</span>
      </router-link>
    </div>
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
      <el-button type="primary" @click="fetchProducts">重试</el-button>
    </div>
    <div v-else-if="products.length === 0" class="empty">
      <el-empty description="暂无商品" />
    </div>
    <div v-else class="product-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <img :src="product.imageUrl" class="product-image" @click="viewProductDetail(product.id)">
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="price">¥{{ product.price }}</p>
          <div class="actions">
            <el-button type="primary" @click="addToCart(product)">加入购物车</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { productApi } from '@/api/product';
import { cartApi } from '@/api/cart';
import { ElMessage } from 'element-plus';

export default {
  name: 'MallView',
  setup() {
    const router = useRouter();
    const products = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const cartItemCount = ref(0);

    const fetchProducts = async () => {
      loading.value = true;
      error.value = null;
      try {
        console.log('Fetching products...');
        const response = await productApi.getAllProducts();
        console.log('Products response:', response);
        products.value = response.data;
      } catch (err) {
        console.error('Error fetching products:', err);
        error.value = err.response?.data?.message || '获取商品列表失败';
        ElMessage.error(error.value);
      } finally {
        loading.value = false;
      }
    };

    const fetchCartCount = async () => {
      try {
        const response = await cartApi.getCart();
        cartItemCount.value = response.data.items.reduce((total, item) => total + item.quantity, 0);
      } catch (err) {
        console.error('Error fetching cart count:', err);
      }
    };

    const viewProductDetail = (productId) => {
      console.log('Viewing product detail:', productId);
      router.push(`/home/product/${productId}`);
    };

    const addToCart = async (product) => {
      try {
        console.log('Adding product to cart:', product);
        await cartApi.addToCart(product.id, 1);
        ElMessage.success('已添加到购物车');
        await fetchCartCount();
      } catch (err) {
        console.error('Error adding to cart:', err);
        ElMessage.error(err.response?.data?.message || '添加到购物车失败');
      }
    };

    onMounted(async () => {
      await fetchProducts();
      await fetchCartCount();
    });

    return {
      products,
      loading,
      error,
      cartItemCount,
      viewProductDetail,
      addToCart
    };
  }
};
</script>

<style scoped>
.mall-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.mall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-icon {
  position: relative;
  text-decoration: none;
  color: #333;
  font-size: 24px;
}

.cart-count {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #f56c6c;
  color: white;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 12px;
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

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.actions {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}
</style> 