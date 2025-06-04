# 团队项目答辩报告

##  Team Report 

### 1. Metrics 

我们使用以下指标来描述项目的复杂度：

#### Lines of Code
根据CLOC工具的统计结果，我们项目的总有效代码行数（不包括空行和注释）为**15174**行，其中Vue组件和Java代码占主要部分。

以下是详细的统计数据：
```
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Vuejs Component                 43           1232            426           8477
Java                           105           1317            425           5896
JavaScript                      15            111             56            693
SQL                              3             10             27             49
YAML                             1              3              0             38
Properties                       1             10              7             21
-------------------------------------------------------------------------------
SUM:                           168           2683            941          15174
```

#### Number of source files
根据CLOC工具的统计结果，我们项目总共包含**168**个源代码文件，包括105个Java文件、43个Vue组件文件、15个JavaScript文件、3个SQL文件以及其他配置和脚本文件。

#### Cyclomatic complexity
在本项目中，我们使用PMD工具来计算代码的圈复杂度(Cyclomatic Complexity)。我们在custom-rules.xml文件中设置了圈复杂度的阈值为10，这意味着当方法的圈复杂度超过10时，PMD将会生成警告。通过控制代码复杂度，我们能够确保代码的可维护性和可理解性。

```xml
<rule ref="category/java/design.xml/CyclomaticComplexity">
    <properties>
        <property name="reportLevel" value="10"/>
    </properties>
</rule>
```

#### Number of dependencies
根据Maven依赖树分析，我们的项目共有**67**个依赖，主要包括：

- 核心框架：Spring Boot (3.2.3)及其相关模块，如Web、Security、Thymeleaf、Data JPA等
- 数据库：PostgreSQL (42.6.1)
- 认证：JWT相关库 (jjwt-api, jjwt-impl, jjwt-jackson)
- 测试：JUnit Jupiter和相关测试工具
- 开发工具：Lombok简化代码开发
- 其他工具库：如Commons IO等

以下是主要的一级依赖：
```
- org.springframework.boot:spring-boot-starter
- org.springframework.boot:spring-boot-starter-test
- org.springframework.boot:spring-boot-starter-web
- org.springframework.boot:spring-boot-starter-thymeleaf
- org.springframework.boot:spring-boot-starter-data-jpa
- org.postgresql:postgresql
- org.springframework.boot:spring-boot-starter-security
- org.springframework.boot:spring-boot-starter-validation
- org.projectlombok:lombok
- io.jsonwebtoken:jjwt-api
- io.jsonwebtoken:jjwt-impl
- io.jsonwebtoken:jjwt-jackson
- commons-io:commons-io
```

### 2. Documentation 

#### Documentation for end users
<!-- 在此处提供面向最终用户的文档链接或截图和简短描述 -->
<!-- 如README或wiki页面，提供软件使用的必要信息或步骤 -->

#### Documentation for developers
<!-- 在此处提供面向开发者的API文档链接或截图和简短描述 -->
<!-- 这些文档应帮助开发者、合作者或潜在的未来贡献者了解设计、目的和重要代码实体的实现 -->

### 3. Tests 

#### Testing Technologies and Approaches
<!-- 描述用于自动测试项目的技术/工具/框架/方法 -->

#### Test Source Code or Artifacts
<!-- 提供测试源代码或相关制品的URL链接或截图 -->

#### Test Effectiveness
<!-- 简要回答和解释测试的有效性，可以使用测试覆盖率报告来展示 -->

### 4. Build 

#### Build Technologies and Approaches
<!-- 描述用于构建项目的技术/工具/框架/方法 -->

#### Build Tasks
<!-- 描述构建中执行的任务，除了编译和打包外，还可以考虑添加linters、测试、测试报告生成和文档生成 -->

#### Build Artifacts
<!-- 描述成功构建产生的最终制品 -->

#### Build Configuration Files
<!-- 提供构建文件(如pom.xml)或相关制品/脚本的URL链接或截图 -->

### 5. Deployment 

#### Containerization Technologies and Approaches
<!-- 描述用于容器化项目的技术/工具/框架/方法 -->

#### Containerization Scripts or Artifacts
<!-- 提供用于容器化的脚本或相关制品(如Dockerfile)的URL链接或截图 -->

#### Proof of Successful Containerization
<!-- 提供成功容器化的证明(如成功的docker build截图) -->
