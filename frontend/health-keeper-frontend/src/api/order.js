import api from './config';

export const orderApi = {
    createOrder: (orderData) => {
        console.log('Creating order with data:', orderData);
        return api.post('/orders/checkout', orderData);
    },

    getMyOrders: () => {
        console.log('Fetching my orders...');
        return api.get('/orders')
            .then(response => {
                console.log('Orders response:', response);
                return response;
            })
            .catch(error => {
                console.error('Error fetching orders:', error);
                throw error;
            });
    },

    getOrderById: (id) => {
        console.log('Fetching order by id:', id);
        return api.get(`/orders/${id}`);
    },

    updateOrderStatus: (orderId, status) => {
        console.log('Updating order status:', { orderId, status });
        return api.put(`/orders/${orderId}/status`, null, {
            params: { status }
        });
    },

    deleteOrder: (orderId) => {
        console.log('Deleting order:', orderId);
        return api.delete(`/orders/${orderId}`);
    },

    getSellerOrders: () => {
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.get('/orders/seller', {
            headers: {
                'Authorization': `Bearer ${token}`
    }
        });
    },

    shipOrder: (orderId, trackingNumber) => {
        console.log('Shipping order:', { orderId, trackingNumber });
        return api.post(`/orders/${orderId}/ship`, { trackingNumber });
    },

    confirmOrderReceipt: (orderId) => {
        console.log('Confirming order receipt:', orderId);
        return api.post(`/orders/${orderId}/confirm`);
    }
}; 