/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me generate the 
 * front-end ui of my hand-drawn design as shown 
 * in the figure" 
 * and directly copied the code from its response.
 */
import api from './config'

class TrainingService {
  // 获取用户的所有训练计划
  getUserSchedule() {
    return api.get('/training/schedule')
  }

  // 获取指定日期的训练计划
  getDailySchedule(date) {
    return api.get(`/training/schedule/${date}`)
  }

  // 添加训练计划项
  addScheduleItem(scheduleData) {
    return api.post('/training/schedule', scheduleData)
  }

  // 更新训练计划项
  updateScheduleItem(itemId, updateData) {
    return api.put(`/training/schedule/item/${itemId}`, updateData)
  }

  // 删除训练计划项
  deleteScheduleItem(itemId) {
    return api.delete(`/training/schedule/item/${itemId}`)
  }

  // 批量获取日期范围内的训练计划
  getScheduleRange(startDate, endDate) {
    return api.get('/training/schedule/range', {
      params: {
        startDate,
        endDate
      }
    })
  }

  // 标记训练计划项为已完成
  markAsCompleted(itemId) {
    return api.put(`/training/schedule/item/${itemId}/complete`, { completed: true })
  }

  // 标记训练计划项为未完成
  markAsIncomplete(itemId) {
    return api.put(`/training/schedule/item/${itemId}/complete`, { completed: false })
  }
}

export default new TrainingService() 