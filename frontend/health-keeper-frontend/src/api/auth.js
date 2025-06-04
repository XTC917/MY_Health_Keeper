import api from './config';

class AuthService {
  login(username, password) {
    return api
        .post('/auth/login', {
          username,
          password
        })
        .then(response => {
          if (response.data.token) {// 关键修改点：单独保存 token 和用户信息
            localStorage.setItem('user', JSON.stringify(response.data));// 1. 保存完整用户信息到 localStorage

            // 2. 单独保存 token（便于其他组件直接读取）
            localStorage.setItem('token', response.data.token);

            // 3. 输出调试信息
            console.log('登录成功，令牌已保存:', response.data.token);

          }
          return response.data;
        });
  }

  logout() {
    localStorage.removeItem('user');

    localStorage.removeItem('token');// 登出时同时清除 user 和 token
  }

  register(username, email, password, phone) {
    console.log('Sending register request with data:', { username, email, phone });
    return api.post('/auth/register', {
      username,
      email,
      password,
      phone
    }).then(response => {
      console.log('Full register response:', response);
      console.log('Response status:', response.status);
      console.log('Response headers:', response.headers);
      console.log('Response data:', response.data);
      
      if (response.data && response.status === 200) {
        return response.data;
      }
      throw new Error(response.data?.message || '注册失败');
    }).catch(error => {
      console.error('Register error:', error);
      if (error.response) {
        console.error('Error response data:', error.response.data);
        console.error('Error response status:', error.response.status);
        console.error('Error response headers:', error.response.headers);
        throw error.response.data;
      }
      throw error;
    });
  }

  getCurrentUser() {
    const user = JSON.parse(localStorage.getItem('user'));
    return user && user.token ? user : null;
  }

  isAuthenticated() {
    const user = this.getCurrentUser();
    return !!user && !!user.token;
  }
}

export default new AuthService(); 