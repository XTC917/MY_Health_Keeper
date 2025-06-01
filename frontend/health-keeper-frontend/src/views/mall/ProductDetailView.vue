<template>
  <div class="product-detail-container">
    <!-- 返回按钮 -->
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

    <div v-else-if="product" class="product-detail">
      <div class="product-header">
        <h1>{{ product.name }}</h1>
        <el-tag :type="product.isAvailable ? 'success' : 'danger'">
          {{ product.isAvailable ? '有货' : '缺货' }}
        </el-tag>
      </div>

      <div class="product-content">
        <div class="product-image">
          <img :src="product.imageUrl || '/default-product.png'" :alt="product.name" />
        </div>

        <div class="product-info">
          <div class="price">
            <span class="label">价格：</span>
            <span class="amount">¥{{ product.price }}</span>
          </div>

          <div class="stock">
            <span class="label">库存：</span>
            <span>{{ product.stock }}</span>
          </div>

          <div class="category">
            <span class="label">分类：</span>
            <span>{{ product.category }}</span>
          </div>

          <div class="brand" v-if="product.brand">
            <span class="label">品牌：</span>
            <span>{{ product.brand }}</span>
          </div>

          <div class="description">
            <h3>商品描述</h3>
            <p>{{ product.description }}</p>
          </div>

          <div class="quantity">
            <span class="label">数量：</span>
            <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="product.stock"
              size="large"
              @change="handleQuantityChange"
            />
          </div>

          <div class="actions">
            <el-button 
              type="primary" 
              size="large" 
              :disabled="product.stock <= 0"
              @click="addToCart"
            >
              加入购物车
            </el-button>
            <el-button 
              type="success" 
              size="large" 
              :disabled="product.stock <= 0"
              @click="buyNow"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改商品信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改商品信息"
      width="500px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="商品价格">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="商品库存">
          <el-input-number v-model="editForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            class="upload-demo"
            action="/api/files/upload"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :headers="uploadHeaders"
          >
            <el-button type="primary">上传图片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg/png 文件，且不超过 2MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateProduct" :loading="updating">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { productApi } from '@/api/product';
import { cartApi } from '@/api/cart';
import { ElMessage } from 'element-plus';

export default {
  name: 'ProductDetailView',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const product = ref(null);
    const loading = ref(true);
    const error = ref(null);
    const quantity = ref(1);
    const isOwner = ref(false);
    const editDialogVisible = ref(false);
    const editForm = ref({
      name: '',
      price: 0,
      stock: 0,
      description: '',
      imageUrl: ''
    });
    const updating = ref(false);
    const uploadHeaders = ref({
      Authorization: `Bearer ${localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).token : ''}`
    });

    const fetchProduct = async () => {
      try {
        loading.value = true;
        const response = await productApi.getProductById(route.params.id);
        product.value = response.data;
        isOwner.value = checkIfOwner();
      } catch (err) {
        console.error('Error fetching product:', err);
        error.value = '获取商品详情失败';
      } finally {
        loading.value = false;
      }
    };

    const checkIfOwner = () => {
      const user = JSON.parse(localStorage.getItem('user'));
      return user && product.value && user.id === product.value.userId;
    };

    const showEditDialog = () => {
      editForm.value = {
        name: product.value.name,
        price: product.value.price,
        stock: product.value.stock,
        description: product.value.description,
        imageUrl: product.value.imageUrl
      };
      editDialogVisible.value = true;
    };

    const updateProduct = async () => {
      try {
        updating.value = true;
        await productApi.updateProduct(product.value.id, editForm.value);
        ElMessage.success('商品信息更新成功');
        editDialogVisible.value = false;
        await fetchProduct();
      } catch (error) {
        console.error('Error updating product:', error);
        ElMessage.error('更新商品信息失败: ' + (error.response?.data?.message || error.message));
      } finally {
        updating.value = false;
      }
    };

    const handleUploadSuccess = (response) => {
      editForm.value.imageUrl = response.url;
      ElMessage.success('图片上传成功');
    };

    const beforeUpload = (file) => {
      const isImage = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        ElMessage.error('只能上传图片文件!');
        return false;
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!');
        return false;
      }
      return true;
    };

    const handleQuantityChange = (value) => {
      if (value > product.value.stock) {
        quantity.value = product.value.stock;
        ElMessage.warning('不能超过库存数量');
      }
    };

    const addToCart = async () => {
      try {
        if (quantity.value <= 0) {
          ElMessage.warning('请选择购买数量');
          return;
        }
        if (quantity.value > product.value.stock) {
          ElMessage.warning('购买数量不能超过库存数量');
          return;
        }
        console.log('Adding product to cart:', product.value.id, quantity.value);
        await cartApi.addToCart(product.value.id, quantity.value);
        ElMessage.success('已添加到购物车');
      } catch (err) {
        console.error('Error adding to cart:', err);
        ElMessage.error(err.response?.data?.message || '添加到购物车失败');
      }
    };

    const buyNow = async () => {
      try {
        if (quantity.value <= 0) {
          ElMessage.warning('请选择购买数量');
          return;
        }
        if (quantity.value > product.value.stock) {
          ElMessage.warning('购买数量不能超过库存数量');
          return;
        }
        // 将商品信息存储到 sessionStorage
        const orderData = {
          items: [{
            productId: product.value.id,
            quantity: quantity.value,
            productName: product.value.name,
            price: product.value.price,
            imageUrl: product.value.imageUrl
          }]
        };
        sessionStorage.setItem('directOrder', JSON.stringify(orderData));
        // 跳转到结算页面
        router.push('/home/checkout');
      } catch (err) {
        console.error('Error buying product:', err);
        ElMessage.error(err.response?.data?.message || '购买失败');
      }
    };

    const goBack = () => {
      router.back();
    };

    onMounted(() => {
      fetchProduct();
    });

    return {
      product,
      loading,
      error,
      quantity,
      isOwner,
      editDialogVisible,
      editForm,
      updating,
      uploadHeaders,
      addToCart,
      buyNow,
      goBack,
      showEditDialog,
      updateProduct,
      handleUploadSuccess,
      beforeUpload,
      handleQuantityChange
    };
  }
};
</script>

<style scoped>
.product-detail-container {
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

.product-detail {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.product-content {
  display: flex;
  gap: 40px;
}

.product-image {
  flex: 1;
}

.product-image img {
  width: 100%;
  max-width: 500px;
  height: auto;
  border-radius: 8px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.price {
  font-size: 24px;
  color: #f56c6c;
}

.price .amount {
  font-weight: bold;
}

.label {
  color: #666;
  margin-right: 10px;
}

.description {
  margin-top: 20px;
}

.description h3 {
  margin-bottom: 10px;
}

.quantity {
  display: flex;
  align-items: center;
  gap: 10px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 20px;
}

.actions .el-button {
  flex: 1;
  width: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 