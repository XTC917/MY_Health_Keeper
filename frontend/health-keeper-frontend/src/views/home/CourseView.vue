const enrollCourse = (courseId) => {
  try {
    // 获取已加入的课程ID列表
    const enrolledCourseIds = JSON.parse(localStorage.getItem('enrolledCourseIds') || '[]')
    
    // 如果课程已经加入，则移除
    if (enrolledCourseIds.includes(courseId)) {
      const newEnrolledCourseIds = enrolledCourseIds.filter(id => id !== courseId)
      localStorage.setItem('enrolledCourseIds', JSON.stringify(newEnrolledCourseIds))
      ElMessage.success('已取消加入课程')
    } else {
      // 如果课程未加入，则添加
      enrolledCourseIds.push(courseId)
      localStorage.setItem('enrolledCourseIds', JSON.stringify(enrolledCourseIds))
      ElMessage.success('成功加入课程')
    }
    
    // 更新课程列表的加入状态
    courses.value = courses.value.map(course => {
      if (course.id === courseId) {
        return {
          ...course,
          isEnrolled: !course.isEnrolled
        }
      }
      return course
    })
  } catch (error) {
    console.error('Error enrolling course:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 在 loadCourses 函数中添加已加入课程的判断
const loadCourses = () => {
  try {
    const enrolledCourseIds = JSON.parse(localStorage.getItem('enrolledCourseIds') || '[]')
    const storedCourses = JSON.parse(localStorage.getItem('courses') || '[]')
    courses.value = storedCourses.map(course => ({
      ...course,
      isEnrolled: enrolledCourseIds.includes(course.id)
    }))
  } catch (error) {
    console.error('Error loading courses:', error)
    ElMessage.error('加载课程失败')
  }
} 