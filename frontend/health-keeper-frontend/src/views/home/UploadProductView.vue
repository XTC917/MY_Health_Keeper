<template>
  <div class="upload-product">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
    </div>
    
    <div class="upload-container">
      <h2>添加商品</h2>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="upload-form"
      >
        <el-form-item label="商品图片" prop="image">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleImageChange"
            :limit="1"
            accept="image/*"
          >
            <el-button type="primary">选择图片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传商品图片，支持jpg/png格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-form-item label="商品价格" prop="price">
          <el-input-number 
            v-model="form.price" 
            :min="0" 
            :precision="2" 
            :step="0.1"
            placeholder="请输入商品价格"
          />
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            上传商品
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

export default {
  name: 'UploadProductView',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    
    const form = reactive({
      image: null,
      name: '',
      price: 0,
      description: ''
    })
    
    const rules = {
      image: [
        { required: true, message: '请上传商品图片', trigger: 'change' }
      ],
      name: [
        { required: true, message: '请输入商品名称', trigger: 'blur' }
      ],
      price: [
        { required: true, message: '请输入商品价格', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入商品描述', trigger: 'blur' }
      ]
    }
    
    // 返回上一页
    const goBack = () => {
      router.push('/home/my-products')
    }
    
    // 处理图片上传
    const handleImageChange = (file) => {
      form.image = file.raw
    }
    
    // 提交表单
    const submitForm = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        
        if (!form.image) {
          ElMessage.error('请上传商品图片')
          return
        }
        
        loading.value = true
        
        // 创建商品对象
        const product = {
          id: Date.now().toString(),
          image: URL.createObjectURL(form.image),
          name: form.name,
          price: form.price,
          description: form.description,
          seller: JSON.parse(localStorage.getItem('user') || '{}').username || '匿名用户'
        }
        
        // 保存到我的商品列表
        const myProducts = JSON.parse(localStorage.getItem('myProducts') || '[]')
        myProducts.push(product)
        localStorage.setItem('myProducts', JSON.stringify(myProducts))
        
        // 保存到所有商品列表
        const allProducts = JSON.parse(localStorage.getItem('allProducts') || '[]')
        allProducts.push(product)
        localStorage.setItem('allProducts', JSON.stringify(allProducts))
        
        ElMessage.success('商品上传成功')
        router.push('/home/my-products')
      } catch (error) {
        console.error('表单验证失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    return {
      formRef,
      form,
      rules,
      loading,
      goBack,
      handleImageChange,
      submitForm
    }
  }
}
</script>

<style scoped>
.upload-product {
  padding: 1rem;
}

.back-button {
  margin-bottom: 1rem;
}

.upload-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.upload-form {
  margin-top: 2rem;
}

.el-upload__tip {
  color: #666;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}
</style> 