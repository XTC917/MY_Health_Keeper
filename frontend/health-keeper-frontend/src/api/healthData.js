import api from './config'

class HealthDataService {
  // 获取健康数据
  getHealthData(page = 0, size = 100) {
    return api.get(`/health-data/me?page=${page}&size=${size}`)
  }

  // 添加健康数据
  addHealthData(data) {
    return api.post('/health-data/', data)
  }

  // 更新健康数据
  updateHealthData(id, data) {
    return api.put(`/health-data/${id}`, data)
  }

  // 删除健康数据
  deleteHealthData(id) {
    return api.delete(`/health-data/${id}`)
  }

  // 获取健康数据统计
  getHealthStatistics() {
    return api.get('/health-data/me/statistics')
  }

  // 获取特定日期的健康数据
  getHealthDataByDate(date) {
    return api.get(`/health-data/me/date/${date}`)
  }
}

export default new HealthDataService() 