import api from './config'

class CourseService {
  // 创建课程
  createCourse(courseData) {
    return api.post('/courses', courseData)
  }

  // 获取所有课程
  getAllCourses() {
    console.log('Fetching all courses...')
    return api.get('/courses/all')
      .then(response => {
        console.log('Courses response:', response)
        if (!response.data || !Array.isArray(response.data)) {
          throw new Error('Invalid courses data format')
        }
        return response
      })
      .catch(error => {
        console.error('Error fetching courses:', error)
        throw error
      })
  }

  // 获取我创建的课程
  getMyCourses() {
    return api.get('/courses/my')
      .then(response => {
        if (!response.data || !Array.isArray(response.data)) {
          throw new Error('Invalid courses data format')
        }
        return response
      })
  }

  // 获取我加入的课程
  getEnrolledCourses() {
    return api.get('/courses/my-courses')
      .then(response => {
        if (!response.data || !Array.isArray(response.data)) {
          throw new Error('Invalid courses data format')
        }
        return response
      })
  }

  // 加入课程
  enrollCourse(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'))
    }
    return api.post(`/courses/${courseId}/enroll`)
  }

  // 删除课程
  deleteCourse(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'))
    }
    return api.delete(`/courses/${courseId}`)
  }

  // 获取课程详情
  getCourseById(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'))
    }
    return api.get(`/courses/${courseId}`)
  }

  getCourseChapters(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'))
    }
    return api.get(`/courses/${courseId}/chapters`)
  }

  createChapter(courseId, chapterData) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'))
    }
    return api.post(`/courses/${courseId}/chapters`, chapterData)
  }

  updateChapter(courseId, chapterId, chapterData) {
    if (!courseId || !chapterId) {
      return Promise.reject(new Error('Course ID and Chapter ID are required'))
    }
    return api.put(`/courses/${courseId}/chapters/${chapterId}`, chapterData)
  }

  deleteChapter(courseId, chapterId) {
    if (!courseId || !chapterId) {
      return Promise.reject(new Error('Course ID and Chapter ID are required'))
    }
    return api.delete(`/courses/${courseId}/chapters/${chapterId}`)
  }

  reorderChapter(courseId, chapterId, newOrder) {
    if (!courseId || !chapterId) {
      return Promise.reject(new Error('Course ID and Chapter ID are required'))
    }
    return api.put(`/courses/${courseId}/chapters/${chapterId}/reorder`, { newOrder })
  }

  toggleLike(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'))
    }
    return api.post(`/courses/${courseId}/like`)
  }
}

export default new CourseService() 