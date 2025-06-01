import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import Register from '../views/Register.vue'
import MallView from '@/views/mall/MallView.vue'
import ProductDetailView from '@/views/mall/ProductDetailView.vue'
import CartView from '@/views/mall/CartView.vue'
import MyProductsView from '@/views/mall/MyProductsView.vue'
import MyOrdersView from '@/views/mall/MyOrdersView.vue'
import OrderDetailView from '@/views/mall/OrderDetailView.vue'
import UploadProductView from '../views/mall/UploadProductView.vue'

const routes = [
  {
    path: '/',
    redirect: '/home/courses'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    children: [
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('../views/home/CoursesView.vue')
      },
      {
        path: 'course/:id',
        name: 'CourseDetail',
        component: () => import('../views/home/CourseDetailView.vue')
      },
      {
        path: 'training',
        name: 'Training',
        component: () => import('../views/home/TrainingView.vue')
      },
      {
        path: 'friends',
        name: 'Friends',
        component: () => import('../views/home/FriendsView.vue')
      },
      {
        path: 'post-moment',
        name: 'PostMoment',
        component: () => import('../views/home/PostMomentView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/home/ProfileView.vue')
      },
      {
        path: 'health-data',
        name: 'HealthData',
        component: () => import('../views/home/HealthDataView.vue')
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('../views/home/MyCoursesView.vue')
      },
      {
        path: 'upload-course',
        name: 'UploadCourse',
        component: () => import('../views/home/UploadCourseView.vue')
      },
      {
        path: 'mall',
        name: 'mall',
        component: MallView,
        meta: { requiresAuth: true }
      },
      {
        path: 'product/:id',
        name: 'product-detail',
        component: ProductDetailView,
        meta: { requiresAuth: true }
      },
      {
        path: 'cart',
        name: 'cart',
        component: CartView,
        meta: { requiresAuth: true }
      },
      {
        path: 'my-products',
        name: 'my-products',
        component: MyProductsView,
        meta: { requiresAuth: true }
      },
      {
        path: 'my-orders',
        name: 'my-orders',
        component: MyOrdersView,
        meta: { requiresAuth: true }
      },
      {
        path: 'order/:id',
        name: 'order-detail',
        component: OrderDetailView,
        meta: { requiresAuth: true }
      },
      {
        path: 'mall/upload-product',
        name: 'UploadProduct',
        component: UploadProductView,
        meta: { requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/mall/CheckoutView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '',
        redirect: 'courses'
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const token = user.token;
  
  // 定义公开页面
  const publicPages = ['/login', '/register', '/home/courses'];
  
  // 检查当前页面是否需要认证
  const authRequired = !publicPages.some(publicPath => {
    return to.path.startsWith(publicPath);
  });

  // 检查是否需要登录权限
  if (authRequired && !token) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    });
  } else {
    next();
  }
})

export default router 