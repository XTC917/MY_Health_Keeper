import api from './config';

class MomentService {
  getAllMoments() {
    return api.get('/moments');
  }

  getMomentById(id) {
    return api.get(`/moments/${id}`);
  }

  createMoment(momentData) {
    return api.post('/moments', momentData);
  }

  updateMoment(id, momentData) {
    return api.put(`/moments/${id}`, momentData);
  }

  deleteMoment(id) {
    return api.delete(`/moments/${id}`);
  }

  likeMoment(id) {
    return api.post(`/moments/${id}/like`);
  }

  unlikeMoment(id) {
    return api.delete(`/moments/${id}/like`);
  }

  getMyMoments() {
    return api.get('/moments/my');
  }

  getComments(momentId) {
    return api.get(`/moments/${momentId}/comments`);
  }

  addComment(momentId, commentData) {
    return api.post(`/moments/${momentId}/comments`, commentData);
  }

  updateComment(momentId, commentId, commentData) {
    return api.put(`/moments/${momentId}/comments/${commentId}`, commentData);
  }

  deleteComment(momentId, commentId) {
    return api.delete(`/moments/${momentId}/comments/${commentId}`);
  }
}

export default new MomentService(); 