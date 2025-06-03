import api from './config';

class MomentService {
  // 获取好友动态列表
  getFriendsMoments() {
    return api.get('/moments/friends');
  }

  // 获取动态详情
  getMomentById(id) {
    return api.get(`/moments/${id}`);
  }

  // 创建动态
  createMoment(momentData) {
    // 如果是FormData对象，直接使用
    if (momentData instanceof FormData) {
      return api.post('/api/moments', momentData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    }
    
    // 如果不是FormData，创建FormData对象
    const formData = new FormData();
    formData.append('content', momentData.content);
    
    if (momentData.files) {
      momentData.files.forEach(file => {
        formData.append('files', file);
      });
    }
    
    if (momentData.courseId) {
      formData.append('courseId', momentData.courseId);
    }
    
    return api.post('/api/moments', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }

  // 更新动态
  updateMoment(id, momentData) {
    return api.put(`/moments/${id}`, momentData);
  }

  // 删除动态
  deleteMoment(id) {
    return api.delete(`/moments/${id}`);
  }

  // 点赞动态
  likeMoment(id) {
    return api.post(`/moments/${id}/like`);
  }

  // 取消点赞
  unlikeMoment(id) {
    return api.delete(`/moments/${id}/like`);
  }

  // 获取我的动态
  getMyMoments() {
    return api.get('/moments/my');
  }

  // 获取评论列表
  getComments(momentId) {
    return api.get(`/moments/${momentId}/comments`);
  }

  // 添加评论
  addComment(momentId, commentData) {
    return api.post(`/moments/${momentId}/comments`, commentData);
  }

  // 更新评论
  updateComment(momentId, commentId, commentData) {
    return api.put(`/moments/${momentId}/comments/${commentId}`, commentData);
  }

  // 删除评论
  deleteComment(momentId, commentId) {
    return api.delete(`/moments/${momentId}/comments/${commentId}`);
  }
}

export default new MomentService(); 