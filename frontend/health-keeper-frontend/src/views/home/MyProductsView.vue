<template>
  <div class="my-products-page">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
    </div>
    
    <div class="header">
      <h2>我上架的商品</h2>
      <button class="upload-btn" @click="goToUpload">添加商品</button>
    </div>
    
    <div v-if="products.length === 0" class="empty-state">
      <img src="https://via.placeholder.com/100" alt="empty" class="empty-icon">
      <p>暂无上架的商品</p>
    </div>
    
    <div v-else class="product-list">
      <div class="product-card" v-for="product in products" :key="product.id">
        <img :src="product.image" :alt="product.name" class="product-image">
        <div class="product-info">
          <div class="product-header">
            <h3>{{ product.name }}</h3>
            <button class="delete-btn" @click="confirmDelete(product)">删除</button>
          </div>
          <p class="price">¥{{ product.price }}</p>
          <p class="description">{{ product.description }}</p>
        </div>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
      :before-close="handleClose"
    >
      <span>确定要删除商品 "{{ selectedProduct?.name }}" 吗？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteProduct">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

export default {
  name: 'MyProductsView',
  setup() {
    const router = useRouter()
    const products = ref([])
    const deleteDialogVisible = ref(false)
    const selectedProduct = ref(null)
    
    // 返回上一页
    const goBack = () => {
      router.push('/home/profile')
    }
    
    // 加载商品
    const loadProducts = () => {
      const savedProducts = JSON.parse(localStorage.getItem('myProducts') || '[]')
      products.value = savedProducts
    }
    
    // 跳转到上传页面
    const goToUpload = () => {
      router.push('/home/upload-product')
    }
    
    // 确认删除
    const confirmDelete = (product) => {
      selectedProduct.value = product
      deleteDialogVisible.value = true
    }
    
    // 关闭对话框
    const handleClose = () => {
      deleteDialogVisible.value = false
      selectedProduct.value = null
    }
    
    // 删除商品
    const deleteProduct = () => {
      if (!selectedProduct.value) return
      
      // 从我的商品列表中移除选中的商品
      products.value = products.value.filter(product => product.id !== selectedProduct.value.id)
      
      // 更新myProducts localStorage
      localStorage.setItem('myProducts', JSON.stringify(products.value))

      // 从所有商品中移除该商品
      const allProducts = JSON.parse(localStorage.getItem('allProducts') || '[]')
      const updatedAllProducts = allProducts.filter(product => product.id !== selectedProduct.value.id)
      localStorage.setItem('allProducts', JSON.stringify(updatedAllProducts))
      
      // 关闭对话框并重置选中商品
      deleteDialogVisible.value = false
      selectedProduct.value = null
      
      // 显示成功消息
      ElMessage.success('商品删除成功')
    }
    
    // 在组件挂载时加载商品
    onMounted(() => {
      loadProducts()
    })
    
    return {
      products,
      goBack,
      deleteDialogVisible,
      selectedProduct,
      loadProducts,
      goToUpload,
      confirmDelete,
      handleClose,
      deleteProduct
    }
  }
}
</script>

<style scoped>
.my-products-page {
  padding: 1rem;
}

.back-button {
  margin-bottom: 1rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.upload-btn {
  padding: 0.5rem 1rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.empty-icon {
  margin-bottom: 1rem;
}

.product-list {
  display: grid;
  gap: 1rem;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
}

.product-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 1rem;
  flex: 1;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.delete-btn {
  padding: 0.25rem 0.5rem;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.delete-btn:hover {
  background-color: #ff7875;
}

.price {
  color: #ff4d4f;
  font-size: 1.2rem;
  font-weight: bold;
  margin: 0.5rem 0;
}

.description {
  color: #666;
  font-size: 0.9rem;
  margin: 0.25rem 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}
</style> 