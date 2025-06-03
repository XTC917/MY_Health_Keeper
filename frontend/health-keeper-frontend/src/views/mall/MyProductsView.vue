<template>
  <div class="my-products-container">
    <div class="header">
      <h1>我上架的商品</h1>
      <div class="header-buttons">
      <el-button type="primary" @click="showAddProductDialog">添加商品</el-button>
        <el-button type="success" @click="viewOrders">查看订单</el-button>
      </div>
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
      <el-empty description="暂无上架的商品">
        <el-button type="primary" @click="showAddProductDialog">添加商品</el-button>
      </el-empty>
    </div>
    <div v-else class="product-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <img :src="product.imageUrl || '/default-product.png'" :alt="product.name" class="product-image">
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="price">¥{{ product.price }}</p>
          <p class="stock">库存: {{ product.stock }}</p>
          <div class="product-actions">
            <el-button type="primary" @click="viewProductDetail(product.id)">查看详情</el-button>
            <el-button type="warning" @click="showEditDialog(product)">编辑</el-button>
            <el-button type="danger" @click="deleteProduct(product.id)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="添加商品" width="500px">
      <el-form :model="newProduct" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="newProduct.name" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="newProduct.description" type="textarea" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="newProduct.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="newProduct.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            class="upload-demo"
            action="/api/upload"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addProduct">确定</el-button>
        </span>
      </template>
    </el-dialog>

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

    <!-- 订单对话框 -->
    <el-dialog
      v-model="ordersDialogVisible"
      title="商品订单"
      width="80%"
    >
      <div v-if="loadingOrders" class="loading">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="ordersError" class="error">
        <el-alert
          title="加载失败"
          type="error"
          :description="ordersError"
          show-icon
        />
        <el-button type="primary" @click="fetchOrders">重试</el-button>
      </div>
      <div v-else-if="orders.length === 0" class="empty">
        <el-empty description="暂无订单" />
      </div>
      <div v-else class="orders-list">
        <el-table :data="orders" style="width: 100%">
          <el-table-column prop="id" label="订单号" width="120" />
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="scope">
              {{ new Date(scope.row.createdAt).toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <div v-for="item in scope.row.items" :key="item.id" class="order-item">
                <div class="order-item-info">
                  <img :src="item.productImage" :alt="item.productName" class="order-item-image">
                  <div class="order-item-details">
                    <div class="order-item-name">{{ item.productName }}</div>
                    <div class="order-item-quantity">数量: {{ item.quantity }}</div>
                    <div class="order-item-price">单价: ¥{{ item.productPrice }}</div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="总金额" width="120">
            <template #default="scope">
              ¥{{ scope.row.totalAmount }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button type="primary" @click="viewOrderDetail(scope.row.id)">
                查看详情
              </el-button>
              <el-button 
                v-if="scope.row.status === 'PAID'"
                type="success" 
                @click="showShipDialog(scope.row)"
              >
                去发货
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="订单发货"
      width="500px"
    >
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流单号" required>
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="shipOrder" :loading="shipping">
            确认发货
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { productApi } from '@/api/product';
import { orderApi } from '@/api/order';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { uploadFile, deleteFile } from '@/utils/supabase';

export default {
  name: 'MyProductsView',
  setup() {
    const router = useRouter();
    const products = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const dialogVisible = ref(false);
    const newProduct = ref({
      name: '',
      description: '',
      price: 0,
      stock: 0,
      imageUrl: '',
      category: '未分类',
      brand: '无品牌'
    });
    const editDialogVisible = ref(false);
    const editForm = ref({
      id: null,
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
    const ordersDialogVisible = ref(false);
    const orders = ref([]);
    const loadingOrders = ref(false);
    const ordersError = ref(null);
    const shipDialogVisible = ref(false);
    const shipForm = ref({
      orderId: null,
      trackingNumber: ''
    });
    const shipping = ref(false);

    const fetchProducts = async () => {
      loading.value = true;
      error.value = null;
      try {
        console.log('Fetching my products...');
        const response = await productApi.getMyProducts();
        console.log('My products response:', response);
        products.value = response.data;
      } catch (error) {
        console.error('Error fetching my products:', error);
        error.value = error.response?.data?.message || '获取商品列表失败';
        ElMessage.error(error.value);
      } finally {
        loading.value = false;
      }
    };

    const showAddProductDialog = () => {
      router.push('/home/mall/upload-product');
    };

    const addProduct = async () => {
      try {
        console.log('Sending product data:', newProduct.value);
        const response = await productApi.createProduct(newProduct.value);
        console.log('Server response:', response);
        dialogVisible.value = false;
        await fetchProducts();
        ElMessage.success('商品添加成功');
      } catch (error) {
        console.error('Error creating product:', error);
        ElMessage.error('添加商品失败');
      }
    };

    const viewProductDetail = (productId) => {
      router.push(`/home/product/${productId}`);
    };

    const deleteProduct = async (productId) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除这个商品吗？如果商品已被购买，将标记为下架状态。',
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        );
        await productApi.deleteProduct(productId);
        await fetchProducts();
        ElMessage.success('商品已成功处理');
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting product:', error);
          ElMessage.error(error.response?.data?.message || '删除商品失败');
        }
      }
    };

    const showEditDialog = (product) => {
      editForm.value = {
        id: product.id,
        name: product.name,
        price: product.price,
        stock: product.stock,
        description: product.description,
        imageUrl: product.imageUrl
      };
      editDialogVisible.value = true;
    };

    const updateProduct = async () => {
      try {
        updating.value = true;
        await productApi.updateProduct(editForm.value.id, editForm.value);
        ElMessage.success('商品信息更新成功');
        editDialogVisible.value = false;
        await fetchProducts();
      } catch (error) {
        console.error('Error updating product:', error);
        ElMessage.error('更新商品信息失败: ' + (error.response?.data?.message || error.message));
      } finally {
        updating.value = false;
      }
    };

    const handleUploadSuccess = async (file) => {
      try {
        const url = await uploadFile(file.raw);
        editForm.value.imageUrl = url;
        ElMessage.success('图片上传成功');
      } catch (error) {
        console.error('Error uploading file:', error);
        ElMessage.error('图片上传失败');
      }
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

    const viewOrders = async () => {
      ordersDialogVisible.value = true;
      await fetchOrders();
    };

    const fetchOrders = async () => {
      loadingOrders.value = true;
      ordersError.value = null;
      try {
        const response = await orderApi.getSellerOrders();
        orders.value = response.data;
      } catch (error) {
        console.error('Error fetching orders:', error);
        ordersError.value = error.response?.data?.message || '获取订单列表失败';
        ElMessage.error(ordersError.value);
      } finally {
        loadingOrders.value = false;
      }
    };

    const getStatusType = (status) => {
      const types = {
        'PENDING': 'warning',
        'PAID': 'success',
        'SHIPPED': 'primary',
        'DELIVERED': 'success',
        'CANCELLED': 'danger'
      };
      return types[status] || 'info';
    };

    const getStatusText = (status) => {
      const texts = {
        'PENDING': '待付款',
        'PAID': '已付款',
        'SHIPPED': '已发货',
        'DELIVERED': '已收货',
        'CANCELLED': '已取消'
      };
      return texts[status] || status;
    };

    const viewOrderDetail = (orderId) => {
      router.push(`/home/order/${orderId}`);
    };

    const showShipDialog = (order) => {
      shipForm.value = {
        orderId: order.id,
        trackingNumber: ''
      };
      shipDialogVisible.value = true;
    };

    const shipOrder = async () => {
      if (!shipForm.value.trackingNumber) {
        ElMessage.warning('请输入物流单号');
        return;
      }

      try {
        shipping.value = true;
        await orderApi.shipOrder(shipForm.value.orderId, shipForm.value.trackingNumber);
        ElMessage.success('发货成功');
        shipDialogVisible.value = false;
        await fetchOrders();
      } catch (error) {
        console.error('Error shipping order:', error);
        ElMessage.error(error.response?.data?.message || '发货失败');
      } finally {
        shipping.value = false;
      }
    };

    onMounted(async () => {
      await fetchProducts();
    });

    return {
      products,
      loading,
      error,
      dialogVisible,
      newProduct,
      showAddProductDialog,
      addProduct,
      viewProductDetail,
      deleteProduct,
      editDialogVisible,
      editForm,
      updating,
      uploadHeaders,
      showEditDialog,
      updateProduct,
      handleUploadSuccess,
      beforeUpload,
      ordersDialogVisible,
      orders,
      loadingOrders,
      ordersError,
      viewOrders,
      fetchOrders,
      getStatusType,
      getStatusText,
      viewOrderDetail,
      shipDialogVisible,
      shipForm,
      shipping,
      showShipDialog,
      shipOrder
    };
  }
};
</script>

<style scoped>
.my-products-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-buttons {
  display: flex;
  gap: 10px;
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
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
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
  font-size: 1.2em;
}

.price {
  color: #f56c6c;
  font-size: 1.2em;
  font-weight: bold;
  margin: 10px 0;
}

.stock {
  color: #666;
  margin: 10px 0;
}

.product-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.orders-list {
  margin-top: 20px;
}

.order-item {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.order-item-details {
  flex: 1;
}

.order-item-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.order-item-quantity {
  color: #666;
  font-size: 0.9em;
  margin-bottom: 2px;
}

.order-item-price {
  color: #f56c6c;
  font-size: 0.9em;
}
</style> 