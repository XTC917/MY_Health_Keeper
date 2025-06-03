import axios from 'axios';

const api = axios.create({
  baseURL:   'https://dumoaoivxujbxeanvjsw.supabase.co' || 'http://localhost:8081/api',  // 使用环境变量，如果没有则默认使用localhost
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 300000, // 5分钟超时，适用于大文件上传
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (user && user.token) {
      config.headers.Authorization = `Bearer ${user.token}`;
    }
    
    // 如果是文件上传请求，设置更长的超时时间
    if (config.data instanceof FormData) {
      config.timeout = 300000; // 5分钟
      config.headers['Content-Type'] = 'multipart/form-data';
    }
    
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 检查是否是 HTML 响应
    if (typeof response.data === 'string' && response.data.includes('<!DOCTYPE html>')) {
      console.error('Received HTML response instead of JSON. This might indicate a server error.');
      return Promise.reject({
        response: {
          data: {
            message: '服务器返回了错误的响应格式，请检查后端服务是否正常运行',
            success: false
          },
          status: 500
        }
      });
    }

    if (response.data && response.data.success === false) {
      return Promise.reject({
        response: {
          data: response.data,
          status: response.status
        }
      });
    }
    return response;
  },
  (error) => {
    console.error('Response error:', error);
    
    if (error.response) {
      console.error('Error response data:', error.response.data);
      console.error('Error response status:', error.response.status);
      console.error('Error response headers:', error.response.headers);
      
      // 检查是否是HTML响应
      if (typeof error.response.data === 'string' && error.response.data.includes('<!DOCTYPE html>')) {
        console.error('Received HTML response instead of JSON. This might indicate a server error.');
        return Promise.reject({
          response: {
            data: {
              message: '服务器返回了错误的响应格式，请检查后端服务是否正常运行',
              success: false
            },
            status: 500
          }
        });
      }
      
      if (error.response.status === 401) {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        window.location.href = '/login';
      }
      
      // 如果后端返回了错误消息，直接抛出
      if (error.response.data && (error.response.data.message || error.response.data.error)) {
        return Promise.reject({
          response: {
            data: {
              message: error.response.data.message || error.response.data.error,
              success: false
            },
            status: error.response.status
          }
        });
      }
    }
    
    // 如果是网络错误或超时
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      return Promise.reject({
        response: {
          data: {
            message: '请求超时，请检查网络连接',
            success: false
          },
          status: 408
        }
      });
    }
    
    // 如果没有具体的错误信息，返回一个通用错误
    return Promise.reject({
      response: {
        data: {
          message: '操作失败，请稍后重试',
          success: false
        },
        status: error.response?.status || 500
      }
    });
  }
);

export default api; 