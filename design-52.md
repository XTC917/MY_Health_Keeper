## 软件架构及UI设计

### 一、架构设计

#### 1. 架构图

以下是一个**分层架构图**，展示了项目的核心组件及其交互关系：

```plaintext
前端 (Vue.js)
  - CourseDetailView
  - HealthDataView
  - ...

    |
    | HTTP 请求 (REST API)
    |

后端 (Spring Boot)
  - Controller层
    - AuthController
    - CourseController
    - ...

  |
  
  - Service层
    - CourseService
    - CartService
    - ...

  |

  - Repository层
    - CourseRepository
    - UserRepository
    - ...

  |

  - 数据库 (PostgreSQL)
    - Course
    - User
    - Cart
    - ...

安全模块 (JWT, Spring Security)
  - WebSecurityConfig
  - JwtUtils

```

#### 2. 自然语言描述

##### 2.1 架构概述
该项目采用 **前后端分离的分层架构**，前端使用 Vue.js 构建单页应用（SPA），后端基于 Spring Boot 提供 RESTful API 服务。整个系统围绕健康管理功能设计，目前实现了的核心功能包括用户认证、健康数据管理、商城功能等。

##### 2.2 分层架构的组件及作用
1. **前端（Vue.js）**：
   - 负责用户界面展示和交互，包含多个视图组件（如 `CoursesView.vue`、`TrainingListView.vue`）。
   - 通过 HTTP 请求与后端 REST API 通信，获取数据或提交用户操作。
   - 优点：前端独立开发，易于维护和扩展，支持响应式设计。

2. **后端（Spring Boot）**：
   - **Controller 层**：处理 HTTP 请求，解析前端发送的数据，并调用 Service 层进行业务逻辑处理。每个控制器对应一个功能模块，例如 `CourseController` 负责课程相关操作，`AuthController` 负责用户登录和注册。
   - **Service 层**：实现核心业务逻辑，例如课程创建、购物车管理、订单生成等。Service 层通过调用 Repository 层与数据库交互。
   - **Repository 层**：封装数据库操作，使用 Spring Data JPA 提供便捷的 CRUD 操作。每个实体（如 `Course`、`User`）对应一个 Repository 接口（如 `CourseRepository`、`UserRepository`）。
   - **Entity 和 DTO**：`entity` 包定义数据库表结构（如 `Course.java`、`User.java`），`dto` 包定义数据传输对象（如 `CourseRequest.java`、`UserDTO.java`），用于前后端数据交互，减少不必要的字段暴露。
   - **安全模块**：`security` 包中的 `WebSecurityConfig` 和 `JwtUtils` 实现基于 JWT 的身份验证，`AuthTokenFilter` 拦截请求以验证 token，确保接口安全性。

3. **数据库**：
   - 使用关系型数据库（PostgreSQL)，存储用户、课程、订单、购物车等数据。
   - 包含数据库迁移脚本（如 `V2__create_course_tables.sql`），使用 Flyway 或类似工具管理数据库版本。

##### 2.3 为什么选择这种架构？
1. **前后端分离**：
   - 前端和后端独立开发部署，提高开发效率。
   - 前端可以快速迭代 UI，后端专注于业务逻辑和数据处理。
   - 支持跨平台扩展，例如未来可以开发移动端应用，只需复用后端 API。

2. **分层架构**：
   - 后端采用 Controller-Service-Repository 分层，职责清晰，易于维护和测试。
   - Controller 层隔离 HTTP 请求处理，Service 层专注于业务逻辑，Repository 层封装数据访问，降低耦合性。
   - 通过 DTO 隔离实体与外部接口，增强安全性并优化数据传输。

3. **RESTful API**：
   - 提供标准化的接口（如 GET `/courses`、POST `/orders`），便于前端和其他客户端调用。
   - 支持无状态通信，适合分布式系统扩展。

4. **安全性**：
   - 使用 Spring Security 和 JWT 实现用户认证和授权，确保敏感操作（如订单创建、健康数据访问）受保护。
   - `AuthTokenFilter` 验证每条请求的合法性，防止未授权访问。

### 二、UI设计

![3e19834681e6a6870b4067063e066a85](assets/3e19834681e6a6870b4067063e066a85.JPG)

#### 2.1 **主界面布局（包含五个任务栏）**

- **底部任务栏**：五个模块的按钮（课程广场、训练计划、好友圈、购物商城、我的）。每个按钮的图标和名称都能直观地显示其功能。

#### 2.2 **课程广场界面**

**功能**：展示所有可用的健身课程，包括课程标题、教练信息、时长、难度等。

**UI设计：**

- **课程列表**：每个课程用一个卡片来展示，卡片内展示课程的时长，目标人群，简短描述。
- **课程播放按钮**：点击课程后可以进入详细页面或直接播放视频。

#### 2.3 **训练计划界面**

- 完成中

#### 2.4 **好友圈界面**

- 完成中

#### 2.5 **购物商城界面**

**功能**：展示健身相关商品，用户可以浏览并购买。

**UI设计：**

- **商品卡片**：每个商品用卡片显示，卡片内展示商品图片、名称、价格等。
- **购物车**：用户可以查看和管理已添加的商品。

#### 2.6 **我的界面**

**功能**：展示用户的个人资料、设置等信息。

**UI设计：**

- **个人信息**：显示用户的头像、昵称、创建的课程等信息。
- **设置按钮**：包括修改密码、通知设置等。
- **健身统计**：可以展示用户的总训练时长、消耗的卡路里等数据。
