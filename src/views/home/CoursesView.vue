const handleEnroll = async (courseId) => {
  try {
    const response = await enrollCourse(courseId)
    if (response.data.success) {
      // 更新课程状态
      const course = courses.value.find(c => c.id === courseId)
      if (course) {
        course.isEnrolled = response.data.enrolled
        course.totalStudents = response.data.totalStudents
      }
      ElMessage.success(response.data.enrolled ? '成功加入课程' : '已退出课程')
    } else {
      ElMessage.error(response.data.message || '操作失败，请重试')
    }
  } catch (error) {
    console.error('Error enrolling course:', error)
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('操作失败，请重试')
    }
  }
} 