#!/bin/bash

# Docker构建脚本
echo "开始构建AI启蒙时光Docker镜像..."

# 构建镜像
docker build -t ai-enlightenment-build . || {
    echo "Docker镜像构建失败"
    exit 1
}

echo "Docker镜像构建成功！"
echo "开始在容器中编译APK..."

# 在容器中运行编译
docker run --rm \
    -v $(pwd)/app/build:/app/app/build \
    -v $(pwd)/build:/app/build \
    ai-enlightenment-build \
    ./gradlew clean assembleDebug || {
    echo "APK编译失败"
    exit 1
}

echo "APK编译成功！"

# 检查APK文件
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "APK文件位置: $APK_PATH"
    echo "APK文件大小: $(ls -lh $APK_PATH | awk '{print $5}')"
else
    echo "错误: 未找到APK文件"
    exit 1
fi

echo "构建完成！"