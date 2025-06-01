<template>
  <div class="orders-container">
    <h1>我的订单</h1>
    <el-table :data="orders" style="width: 100%">
      <el-table-column prop="id" label="订单号" width="180" />
      <el-table-column label="订单状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="总金额" width="120">
        <template #default="{ row }">
          ¥{{ row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column label="物流信息" width="200">
        <template #default="{ row }">
          <div v-if="row.status === 'SHIPPED'">
            <p>物流单号：{{ row.trackingNumber }}</p>
            <p>发货时间：{{ formatDate(row.shippedAt) }}</p>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" @click="viewOrderDetail(row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { orderApi } from '@/api/order';
import { ElMessage } from 'element-plus';

export default {
  name: 'MyOrdersView',
  data() {
    return {
      orders: []
    };
  },
  async created() {
    await this.fetchOrders();
  },
  methods: {
    async fetchOrders() {
      try {
        console.log('Fetching orders...');
        const response = await orderApi.getMyOrders();
        console.log('Orders response:', response);
        this.orders = response.data;
        console.log('Orders data:', this.orders);
      } catch (error) {
        console.error('Error fetching orders:', error);
        console.error('Error response:', error.response);
        ElMessage.error('获取订单列表失败: ' + (error.response?.data?.message || error.message));
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
    },
    viewOrderDetail(orderId) {
      this.$router.push(`/home/order/${orderId}`);
    }
  }
};
</script>

<style scoped>
.orders-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
</style> 