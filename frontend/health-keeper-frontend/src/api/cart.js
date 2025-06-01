import api from './config';

export const cartApi = {
    getCart: () => {
        console.log('Fetching cart...');
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.get('/api/cart', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    addToCart: (productId, quantity) => {
        console.log('Adding to cart:', { productId, quantity });
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.post('/api/cart/items', { productId, quantity }, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    updateCartItemQuantity: (itemId, quantity) => {
        console.log('Updating cart item:', { itemId, quantity });
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.put(`/api/cart/items/${itemId}`, null, {
            params: { quantity },
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    removeFromCart: (itemId) => {
        console.log('Removing from cart:', itemId);
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.delete(`/api/cart/items/${itemId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    checkout: () => {
        console.log('Checking out...');
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.post('/api/orders/checkout', null, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
}; 