import api from './config'

class CourseService {
  // 创建课程
  createCourse(courseData) {
    console.log('Creating course with data:', courseData);
    return api.post('/courses', courseData);
  }

  // 获取所有课程
  getAllCourses(query = '', categories = [], levels = []) {
    console.log('Fetching courses...');
    let url = '/courses/filter';
    const params = new URLSearchParams();
    
    if (query) {
      params.append('query', query);
    }
    if (categories.length > 0) {
      categories.forEach(category => params.append('categories', category));
    }
    if (levels.length > 0) {
      levels.forEach(level => params.append('levels', level));
    }
    
    const queryString = params.toString();
    if (queryString) {
      url += `?${queryString}`;
    }
    
    return api.get(url)
      .then(response => {
        console.log('Courses response:', response);
        if (!response.data || !Array.isArray(response.data)) {
          throw new Error('Invalid courses data format');
        }
        return response;
      })
      .catch(error => {
        console.error('Error fetching courses:', error);
        throw error;
      });
  }

  // 获取我创建的课程
  getMyCourses() {
    console.log('Fetching my courses...');
    return api.get('/courses/my')
      .then(response => {
        if (!response.data || !Array.isArray(response.data)) {
          throw new Error('Invalid courses data format');
        }
        return response;
      });
  }

  // 获取我加入的课程
  getEnrolledCourses() {
    console.log('Fetching enrolled courses...');
    return api.get('/courses/my-courses')
      .then(response => {
        if (!response.data || !Array.isArray(response.data)) {
          throw new Error('Invalid courses data format');
        }
        return response;
      });
  }

  // 加入课程
  enrollCourse(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'));
    }
    console.log('Enrolling in course:', courseId);
    return api.post(`/courses/${courseId}/enroll`);
  }

  // 删除课程
  deleteCourse(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'));
    }
    console.log('Deleting course:', courseId);
    return api.delete(`/courses/${courseId}`);
  }

  // 获取课程详情
  getCourseById(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'));
    }
    console.log('Fetching course details:', courseId);
    return api.get(`/courses/${courseId}`);
  }

  // 获取课程章节
  getCourseChapters(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'));
    }
    console.log('Fetching course chapters:', courseId);
    return api.get(`/courses/${courseId}/chapters`);
  }

  // 创建章节
  createChapter(courseId, chapterData) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'));
    }
    console.log('Creating chapter for course:', courseId, chapterData);
    return api.post(`/courses/${courseId}/chapters`, chapterData);
  }

  // 更新章节
  updateChapter(courseId, chapterId, chapterData) {
    if (!courseId || !chapterId) {
      return Promise.reject(new Error('Course ID and Chapter ID are required'));
    }
    console.log('Updating chapter:', chapterId, 'for course:', courseId);
    return api.put(`/courses/${courseId}/chapters/${chapterId}`, chapterData);
  }

  // 删除章节
  deleteChapter(courseId, chapterId) {
    if (!courseId || !chapterId) {
      return Promise.reject(new Error('Course ID and Chapter ID are required'));
    }
    console.log('Deleting chapter:', chapterId, 'from course:', courseId);
    return api.delete(`/courses/${courseId}/chapters/${chapterId}`);
  }

  // 重新排序章节
  reorderChapter(courseId, chapterId, newOrder) {
    if (!courseId || !chapterId) {
      return Promise.reject(new Error('Course ID and Chapter ID are required'));
    }
    console.log('Reordering chapter:', chapterId, 'in course:', courseId);
    return api.put(`/courses/${courseId}/chapters/${chapterId}/reorder`, { newOrder });
  }

  // 点赞课程
  toggleLike(courseId) {
    if (!courseId) {
      return Promise.reject(new Error('Course ID is required'));
    }
    console.log('Toggling like for course:', courseId);
    return api.post(`/courses/${courseId}/like`);
  }
}

export default new CourseService() 