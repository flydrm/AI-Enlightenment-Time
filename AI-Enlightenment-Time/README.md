# AI启蒙时光 - Android教育应用

一款专为3-6岁儿童设计的Android教育应用，通过AI技术提供个性化的学习体验。

## 项目概述

AI启蒙时光是基于Clean Architecture + MVVM架构开发的Android应用，使用Jetpack Compose构建现代化UI，集成多个AI模型提供智能化的教育内容。

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: Clean Architecture + MVVM
- **依赖注入**: Hilt
- **数据库**: Room
- **网络**: Retrofit + OkHttp
- **异步**: Kotlin Coroutines + Flow
- **多媒体**: CameraX, ExoPlayer

## 项目结构

```
app/
├── src/main/java/com/enlightenment/
│   ├── presentation/     # 表现层：UI、ViewModel
│   ├── domain/          # 领域层：业务逻辑、用例
│   ├── data/            # 数据层：仓库实现、数据源
│   ├── ai/              # AI服务：模型管理
│   ├── multimedia/      # 多媒体：相机、音频
│   └── di/              # 依赖注入模块
```

## 主要功能

1. **故事世界** - AI生成个性化故事
2. **拍照识物** - 使用相机识别物体并学习
3. **语音对话** - 与AI小熊猫互动对话
4. **成就系统** - 激励持续学习

## 设计特色

- **温暖童趣风格** - 以红色小熊猫为吉祥物
- **响应式布局** - 支持手机和平板设备
- **儿童友好** - 大按钮、清晰反馈、防误操作

## 构建运行

### 环境要求
- Android Studio Arctic Fox 2021.3.1+
- JDK 11
- Android SDK API 24-33

### 构建步骤
```bash
# 克隆项目
git clone [project-url]
cd AI-Enlightenment-Time

# 构建调试版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

## 开发状态

当前已实现：
- ✅ 基础项目架构
- ✅ Clean Architecture层级结构
- ✅ 主题系统（温暖色彩）
- ✅ 导航系统
- ✅ 主页UI
- ✅ 故事列表界面
- ✅ Room数据库集成
- ✅ 基础动画（小熊猫）

待实现：
- ⏳ AI服务集成
- ⏳ 相机功能
- ⏳ 语音识别
- ⏳ 故事播放器
- ⏳ 完整的响应式布局
- ⏳ 更多动画效果

## 许可证

本项目仅供学习和参考使用。