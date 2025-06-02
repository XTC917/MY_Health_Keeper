import api from './config';

class FriendService {
  // 获取好友列表
  getFriends() {
    return api.get('/friends');
  }

  // 获取待处理的好友请求
  getPendingRequests() {
    return api.get('/friends/requests');
  }

  // 发送好友请求
  sendFriendRequest(username) {
    return api.post('/friends/requests', { username });
  }

  // 处理好友请求
  handleFriendRequest(requestId, action) {
    return api.put(`/friends/requests/${requestId}`, { action: action.toUpperCase() });
  }

  // 删除好友
  deleteFriend(username) {
    return api.delete(`/friends/${encodeURIComponent(username)}`);
  }
}

export default new FriendService(); 