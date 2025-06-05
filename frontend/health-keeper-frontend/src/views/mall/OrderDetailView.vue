<template>
  <div class="order-detail-container">
    <div v-if="order" class="order-detail">
      <div class="order-header">
        <h1>订单详情</h1>
        <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
      </div>
      
      <div class="order-info">
        <p>订单号: {{ order.id }}</p>
        <p>创建时间: {{ formatDate(order.createdAt) }}</p>
        <p>总金额: ¥{{ order.totalAmount }}</p>
        <div v-if="order.status === 'SHIPPED'" class="shipping-info">
          <p>物流单号: {{ order.trackingNumber }}</p>
          <p>发货时间: {{ formatDate(order.shippedAt) }}</p>
        </div>
      </div>

      <div class="order-items">
        <h2>商品列表</h2>
        <el-table :data="order.items" style="width: 100%">
          <el-table-column label="商品图片" width="120">
            <template #default="{ row }">
              <img :src="row.productImage || '/default-product.png'" :alt="row.productName" class="product-image">
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              ¥{{ row.productPrice }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="120" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ row.subtotal }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="order-actions">
        <el-button @click="$router.push('/home/my-orders')">返回订单列表</el-button>
        <el-button 
          v-if="order.status === 'SHIPPED'"
          type="success" 
          @click="confirmReceipt"
        >
          确认收货
        </el-button>
        <el-button 
          type="danger" 
          @click="confirmDelete"
        >
          删除订单
        </el-button>
      </div>
    </div>
    <div v-else class="loading">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script>
import { orderApi } from '@/api/order';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
  name: 'OrderDetailView',
  data() {
    return {
      order: null
    };
  },
  async created() {
    await this.fetchOrder();
  },
  methods: {
    async fetchOrder() {
      try {
        const orderId = this.$route.params.id;
        console.log('Fetching order details for order ID:', orderId);
        const response = await orderApi.getOrderById(orderId);
        console.log('Order details response:', response);
        this.order = response.data;
        console.log('Order data:', this.order);
      } catch (error) {
        console.error('Error fetching order details:', error);
        console.error('Error response:', error.response);
        ElMessage.error('获取订单详情失败: ' + (error.response?.data?.message || error.message));
      }
    },
    async confirmReceipt() {
      try {
        await ElMessageBox.confirm(
          '确认已收到商品？',
          '确认收货',
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'info',
          }
        );
        
        await orderApi.confirmOrderReceipt(this.order.id);
        ElMessage.success('确认收货成功');
        await this.fetchOrder();
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error confirming order receipt:', error);
          ElMessage.error('确认收货失败: ' + (error.response?.data?.message || error.message));
        }
      }
    },
    async confirmDelete() {
      try {
        await ElMessageBox.confirm(
          '确定要删除这个订单吗？此操作不可恢复。',
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        );
        
        await orderApi.deleteOrder(this.order.id);
        ElMessage.success('订单删除成功');
        this.$router.push('/home/my-orders');
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting order:', error);
          ElMessage.error('删除订单失败: ' + (error.response?.data?.message || error.message));
        }
      }
    },
    getStatusType(status) {
      const types = {
        'PENDING': 'warning',
        'PAID': 'success',
        'SHIPPED': 'primary',
        'DELIVERED': 'success',
        'CANCELLED': 'danger'
      };
      return types[status] || 'info';
    },
    getStatusText(status) {
      const texts = {
        'PENDING': '待付款',
        'PAID': '已付款',
        'SHIPPED': '已发货',
        'DELIVERED': '已收货',
        'CANCELLED': '已取消'
      };
      return texts[status] || status;
    },
    formatDate(date) {
      if (!date) return '-';
      return new Date(date).toLocaleString();
    }
  }
};
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.order-info {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.order-info p {
  margin: 10px 0;
}

.shipping-info {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.shipping-info p {
  color: #409eff;
  font-weight: 500;
}

.order-items {
  margin: 20px 0;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.order-actions {
  margin-top: 20px;
  text-align: center;
}

.loading {
  padding: 20px;
}
</style> 