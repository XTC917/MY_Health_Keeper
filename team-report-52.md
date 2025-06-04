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

#### Documentation for End Users

我们的项目为最终用户提供了详细的使用指南和帮助文档，主要包含：

- **README 文件**  
  位于 GitHub 仓库根目录的 `README.md`，包含项目介绍、安装步骤、运行环境配置、快速启动指南和常见问题解决方案。

- **用户手册**  
  Wiki 页面详细介绍了系统各个模块的功能说明、用户注册登录流程、健康数据录入与查询、好友动态浏览和排行榜使用方法。

- **界面操作说明**  
  配合项目演示视频和截图，帮助用户快速理解各页面操作及系统交互。

- **用户文档地址**  
  [https://github.com/sustech-cs304/team-project-25spring-52/blob/Friends/userDocumentation](https://github.com/sustech-cs304/team-project-25spring-52/blob/Friends/userDocumentation)

---

#### Documentation for Developers

项目为开发者和维护人员提供了全面的 API 和架构文档，内容涵盖：

- **系统架构设计文档**  
  包括模块划分、关键类图、数据库 ER 图和安全设计说明。

- **核心类和方法注释**  
  代码中详细注释了重要的服务层、控制器层及工具类（如 `JwtUtils`），帮助理解实现逻辑。

- **接口文档**  
  使用 Swagger 自动生成的 REST API 文档，方便开发者调用和调试后端接口。

- **配置说明**  
  详细介绍了数据库连接配置、JWT 参数说明及安全相关配置。

- **扩展指南**  
  说明如何新增功能模块、调整认证流程及扩展前后端交互。

- **开发者文档地址**  
  [https://github.com/sustech-cs304/team-project-25spring-52/blob/Friends/index.html](https://github.com/sustech-cs304/team-project-25spring-52/blob/Friends/index.html)

### 3. Tests

#### Testing Technologies and Approaches

本项目采用以下技术进行自动化测试：

- **JUnit 5**：编写单元测试和集成测试，验证服务层和数据访问层逻辑。
- **Spring Boot Test**：提供 Spring 上下文支持，测试控制器和服务层集成行为。
- **Mockito**：模拟外部依赖（如数据库），确保单元测试隔离性。
- **Maven**：通过 `mvn test` 运行测试，管理依赖和测试生命周期。
- **JaCoCo**：生成测试覆盖率报告，评估代码覆盖程度。

测试方法包括：

- **单元测试**：测试服务层逻辑，覆盖核心功能和异常处理。
- **集成测试**：验证组件协作，连接真实或模拟数据库。
- **Mock MVC 测试**：测试 REST API 端点，确保正确响应。

#### Test Source Code or Artifacts

测试代码位于 `src/test/java`，包括服务层（`UserServiceTest`）和控制器（`UserControllerTest`）的单元测试及 Mock MVC 测试。代码已上传至 GitHub：

- 测试代码链接：[team-project-25spring-52/src/test/java/com/healthkeeper at Friends · sustech-cs304/team-project-25spring-52](https://github.com/sustech-cs304/team-project-25spring-52/tree/Friends/src/test/java/com/healthkeeper)

#### Test Effectiveness

采用 JaCoCo 生成覆盖率报告，测试用例主要涉及service和controller，覆盖核心功能。报告位于`target/site/jacoco/index.html`，验证了系统的正确性和可靠性。

### 4. Build

#### Build Technologies and Approaches

本项目使用以下技术进行构建：

- **Maven**：核心构建工具，管理依赖、编译、测试和打包。
- **Java 17**：项目开发语言，编译为字节码。
- **Spring Boot Maven Plugin**：支持生成可执行 JAR/WAR 文件，简化部署。
- **JaCoCo**：生成测试覆盖率报告，评估代码质量。
- **Git**：版本控制，管理源代码和构建配置。

构建方法：

- 通过 Maven 生命周期（`compile`、`test`、`package`）自动化完成代码编译、测试和打包。
- 使用标准 Maven 目录结构，源码位于 `src/main/java`，测试代码位于 `src/test/java`。

#### Build Tasks

构建过程执行以下任务：

- **编译**：使用 `mvn compile` 将 Java 源码编译为字节码。
- **测试**：通过 `mvn test` 运行单元测试和集成测试（基于 JUnit 5 和 Spring Boot Test）。
- **测试报告生成**：使用 JaCoCo 插件生成覆盖率报告（`target/site/jacoco/index.html`）。
- **代码质量检查**：集成 Checkstyle 插件，检查代码规范（配置在 `pom.xml`）。
- **打包**：通过 `mvn package` 生成可执行 JAR 文件。

#### Build Artifacts

成功构建产生的制品：

- **可执行 JAR 文件**：`target/java_personal_health_assistant-0.0.1-SNAPSHOT.jar`，包含应用及其依赖，可直接运行。
- **JaCoCo 覆盖率报告**：`target/site/jacoco/index.html`，展示测试覆盖率。

#### Build Configuration Files

构建配置文件为 `pom.xml`，定义依赖、插件和构建任务：

- 链接：[team-project-25spring-52/pom.xml at Friends · sustech-cs304/team-project-25spring-52](https://github.com/sustech-cs304/team-project-25spring-52/blob/Friends/pom.xml)
- 主要配置包括：`spring-boot-starter` 依赖提供核心功能，`spring-boot-starter-web` 支持 REST API，`spring-boot-starter-test` 支持 JUnit 5 测试，`spring-boot-maven-plugin` 生成可执行 JAR，`jacoco-maven-plugin` 生成测试覆盖率报告。

### 5. Deployment 

#### Containerization Technologies and Approaches
<!-- 描述用于容器化项目的技术/工具/框架/方法 -->

#### Containerization Scripts or Artifacts
<!-- 提供用于容器化的脚本或相关制品(如Dockerfile)的URL链接或截图 -->

#### Proof of Successful Containerization
<!-- 提供成功容器化的证明(如成功的docker build截图) -->
