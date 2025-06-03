import request from '@/utils/request'

// 获取评论列表
export function getComments(momentId) {
  return request({
    url: `/api/moments/comments/${momentId}`,
    method: 'get'
  })
}

// 创建评论
export function createComment(momentId, data) {
  return request({
    url: `/api/moments/comments/${momentId}`,
    method: 'post',
    data
  })
}

// 删除评论
export function deleteComment(commentId) {
  return request({
    url: `/api/moments/comments/${commentId}`,
    method: 'delete'
  })
} 