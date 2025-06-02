import axios from 'axios';
import api from './config';
const API_URL = api.baseURL + '/moments';

class MomentService {
  getAllMoments() {
    return axios.get(API_URL);
  }

  getMomentById(id) {
    return axios.get(`${API_URL}/${id}`);
  }

  createMoment(momentData) {
    return axios.post(API_URL, momentData);
  }

  updateMoment(id, momentData) {
    return axios.put(`${API_URL}/${id}`, momentData);
  }

  deleteMoment(id) {
    return axios.delete(`${API_URL}/${id}`);
  }

  likeMoment(id) {
    return axios.post(`${API_URL}/${id}/like`);
  }

  unlikeMoment(id) {
    return axios.delete(`${API_URL}/${id}/like`);
  }

  getMyMoments() {
    return axios.get(`${API_URL}/my`);
  }

  getComments(momentId) {
    return axios.get(`${API_URL}/${momentId}/comments`);
  }

  addComment(momentId, commentData) {
    return axios.post(`${API_URL}/${momentId}/comments`, commentData);
  }

  updateComment(momentId, commentId, commentData) {
    return axios.put(`${API_URL}/${momentId}/comments/${commentId}`, commentData);
  }

  deleteComment(momentId, commentId) {
    return axios.delete(`${API_URL}/${momentId}/comments/${commentId}`);
  }
}

export default new MomentService(); 