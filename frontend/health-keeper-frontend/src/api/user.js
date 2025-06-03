import api from './config'

class UserService {
    // 获取当前用户信息
    getUserInfo() {
        return api.get('/users/info')
    }

    // 更新单个字段（如用户名、地址、性别等）
    updateField(data) {
        return api.patch('/users/profile', data)
    }

    // 上传头像
    uploadAvatar(formData) {
        return api.post('/users/avatar', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    }

    // 修改密码（可选功能）
    updatePassword(oldPassword, newPassword) {
        return api.post('/users/update-password', {
            oldPassword,
            newPassword
        })
    }

    // 注销账号（可选）
    deleteUserAccount() {
        return api.delete('/users/delete')
    }

    getProfile() {
        return api.get('/users/me') // 确保后端接口路径正确
    }

}

export default new UserService()