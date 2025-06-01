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
  }

  // 获取我加入的课程
  getEnrolledCourses() {
    return api.get('/courses/my-courses')
  }

  // 加入课程
  enrollCourse(courseId) {
    return api.post(`/courses/${courseId}/enroll`)
  }

  // 删除课程
  deleteCourse(courseId) {
    return api.delete(`/courses/${courseId}`)
  }

  // 获取课程详情
  getCourseById(courseId) {
    return api.get(`/courses/${courseId}`)
  }

  getCourseChapters(courseId) {
    return api.get(`/courses/${courseId}/chapters`)
  }

  createChapter(courseId, chapterData) {
    return api.post(`/courses/${courseId}/chapters`, chapterData)
  }

  updateChapter(courseId, chapterId, chapterData) {
    return api.put(`/courses/${courseId}/chapters/${chapterId}`, chapterData)
  }

  deleteChapter(courseId, chapterId) {
    return api.delete(`/courses/${courseId}/chapters/${chapterId}`)
  }

  reorderChapter(courseId, chapterId, newOrder) {
    return api.put(`/courses/${courseId}/chapters/${chapterId}/reorder`, { newOrder })
  }

  toggleLike(courseId) {
    return api.post(`/courses/${courseId}/like`)
  }
}

export default new CourseService() 