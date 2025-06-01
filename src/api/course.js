import request from '@/utils/request'

// 加入/退出课程
export function enrollCourse(courseId) {
  return request({
    url: `/api/courses/${courseId}/enroll`,
    method: 'post'
  })
}

// 获取用户已加入的课程
export function getEnrolledCourses() {
  return request({
    url: '/api/courses/my-courses',
    method: 'get'
  })
} 