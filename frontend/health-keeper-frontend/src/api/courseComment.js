import api from './config'

class CourseCommentService {
    // 获取某门课程的所有评论
    getCommentsByCourseId(courseId) {
        return api.get(`/course-comments/course/${courseId}`)
            .then(response => {
                return response.data
            })
            .catch(error => {
                console.error('获取评论失败:', error)
                throw error
            })
    }

    // 创建评论（需用户登录）
    createComment(commentData) {
        return api.post('/course-comments/', commentData)
            .then(response => {
                return response.data
            })
            .catch(error => {
                console.error('创建评论失败:', error)
                throw error
            })
    }

    // 删除评论（只能本人删除）
    deleteComment(commentId) {
        return api.delete(`/course-comments/${commentId}`)
            .then(response => {
                return response.data
            })
            .catch(error => {
                console.error('删除评论失败:', error)
                throw error
            })
    }
}

export default new CourseCommentService()
