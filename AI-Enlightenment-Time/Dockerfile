# 使用Java 11作为基础镜像
FROM openjdk:11-jdk-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 设置Android SDK环境变量
ENV ANDROID_SDK_ROOT=/android-sdk
ENV ANDROID_HOME=/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# 下载并安装Android SDK Command Line Tools
RUN mkdir -p $ANDROID_SDK_ROOT && \
    cd $ANDROID_SDK_ROOT && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip -q commandlinetools-linux-9477386_latest.zip && \
    mkdir -p cmdline-tools/latest && \
    mv cmdline-tools/* cmdline-tools/latest/ || true && \
    rm commandlinetools-linux-9477386_latest.zip

# 接受所有SDK许可证
RUN yes | sdkmanager --licenses || true

# 安装必要的Android SDK组件
RUN sdkmanager --install \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "extras;android;m2repository" \
    "extras;google;m2repository"

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 给gradlew执行权限
RUN chmod +x gradlew

# 设置Gradle参数以使用更少内存
ENV GRADLE_OPTS="-Xmx2048m -Dorg.gradle.daemon=false"

# 预下载依赖
RUN ./gradlew dependencies || true

# 默认命令
CMD ["./gradlew", "assembleDebug"]