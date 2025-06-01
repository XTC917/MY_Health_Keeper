import api from './config';

export const productApi = {
    createProduct: (productData) => {
        console.log('Creating product with data:', productData);
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.post('/products', productData, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    getMyProducts: () => {
        console.log('Fetching my products...');
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.get('/products/my', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    getAllProducts: () => {
        console.log('Fetching all products...');
        return api.get('/products');
    },

    getProductById: (id) => {
        console.log('Fetching product by id:', id);
        return api.get(`/products/${id}`);
    },

    deleteProduct: (id) => {
        console.log('Deleting product:', id);
        const token = JSON.parse(localStorage.getItem('user') || '{}').token;
        return api.delete(`/products/${id}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    updateProduct: (productId, productData) => {
        console.log('Updating product:', productId, productData);
        return api.put(`/products/${productId}`, productData);
    }
}; 