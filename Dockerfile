# 使用 OpenJDK 17 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 Maven 构建文件
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 复制源代码
COPY src ./src

# 安装 Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 构建应用
RUN mvn clean package -DskipTests

# 暴露端口
EXPOSE 8081

# 设置环境变量
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/java_personal_health_assistant-0.0.1-SNAPSHOT.jar"] 