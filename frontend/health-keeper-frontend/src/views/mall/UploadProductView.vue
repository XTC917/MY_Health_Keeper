<template>
  <div class="upload-container">
    <div class="header">
      <el-button type="text" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>上传商品</h2>
    </div>

    <div class="content">
      <el-form
        ref="formRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
        class="upload-form"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品图片" prop="imageUrl">
          <el-upload
            class="image-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <img v-if="productForm.imageUrl" :src="productForm.imageUrl" class="image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" :step="0.1" />
          <span class="unit">元</span>
        </el-form-item>

        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>

        <el-form-item label="商品分类" prop="category">
          <el-select v-model="productForm.category" placeholder="请选择商品分类">
            <el-option label="运动装备" value="SPORTS_GEAR" />
            <el-option label="营养补剂" value="NUTRITION" />
            <el-option label="健身器材" value="FITNESS_EQUIPMENT" />
            <el-option label="健康食品" value="HEALTHY_FOOD" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品品牌" prop="brand">
          <el-input v-model="productForm.brand" placeholder="请输入商品品牌" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { productApi } from '../../api/product'
import api from '../../api/config'

const router = useRouter()
const formRef = ref(null)

const productForm = reactive({
  name: '',
  imageUrl: '',
  price: 0,
  stock: 0,
  category: 'SPORTS_GEAR',
  brand: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传商品图片', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  return true
}

const handleImageUpload = async (options) => {
  const file = options.file
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data) {
      productForm.imageUrl = response.data
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    console.error('Error uploading image:', error)
    ElMessage.error('图片上传失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 创建商品对象
    const productRequest = {
      name: productForm.name,
      description: productForm.description,
      price: productForm.price.toString(),
      stock: productForm.stock,
      imageUrl: productForm.imageUrl.replace('/api', ''),
      category: productForm.category,
      brand: productForm.brand
    }
    
    // 发送到后端API
    const response = await productApi.createProduct(productRequest)
    
    if (response.data) {
      ElMessage.success('商品上传成功')
      router.push('/home/my-products')
    }
  } catch (error) {
    console.error('Error submitting form:', error)
    ElMessage.error(error.response?.data?.message || '提交失败，请检查表单')
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const goBack = () => {
  router.push('/home/my-products')
}
</script>

<style scoped>
.upload-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  margin-left: 20px;
  font-size: 24px;
  color: #303133;
}

.content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.image-uploader:hover {
  border-color: var(--el-color-primary);
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.unit {
  margin-left: 10px;
  color: #606266;
}
</style> 