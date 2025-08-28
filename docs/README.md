# AI启蒙时光 - 技术文档

## 项目概述
AI启蒙时光是一款专为3-6岁儿童设计的Android教育应用，通过AI技术提供个性化的学习体验。

## 文档目录

### 核心文档
1. **[架构设计文档](./architecture-design.md)**
   - 技术架构选型
   - 系统架构设计
   - 模块划分说明
   - 技术栈详情

2. **[UI设计指南](./ui-design-guide.md)**
   - 温暖童趣风格设计规范
   - 响应式布局方案
   - 组件库设计
   - 动画交互规范

3. **[测试策略文档](./test-strategy.md)**
   - 测试架构设计
   - 测试类型和覆盖率
   - 自动化测试方案
   - 测试工具和流程

4. **[开发指南](./development-guide.md)**
   - 环境搭建说明
   - 编码规范
   - 最佳实践
   - 发布流程

## 快速开始

### 环境要求
- Android Studio Arctic Fox 2021.3.1+
- JDK 11
- Android SDK API 24-33
- Kotlin 1.7+

### 项目设置
```bash
# 克隆项目
git clone https://github.com/your-org/AI-Enlightenment-Time.git

# 进入项目目录
cd AI-Enlightenment-Time

# 运行设置脚本
./scripts/setup.sh

# 使用Android Studio打开项目
studio .
```

### 构建运行
```bash
# 运行调试版本
./gradlew installDebug

# 运行所有测试
./gradlew test

# 构建发布版本
./gradlew assembleRelease
```

## 架构概览

### 技术架构
- **架构模式**: Clean Architecture + MVVM
- **UI框架**: Jetpack Compose
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow
- **网络请求**: Retrofit + OkHttp
- **本地存储**: Room + DataStore

### AI模型集成
- **对话生成**: GEMINI-2.5-PRO / GPT-5-PRO
- **图像识别**: Qwen3-Embedding-8B
- **结果排序**: BAAI/bge-reranker-v2-m3
- **图像生成**: grok-4-imageGen

## 团队协作

### 分支策略
- `main`: 生产环境代码
- `develop`: 开发主分支
- `feature/*`: 功能开发分支
- `hotfix/*`: 紧急修复分支

### 提交规范
```
type(scope): subject

- feat: 新功能
- fix: 修复问题
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试相关
- chore: 构建或辅助工具
```

## 相关链接
- [项目需求文档](../README.md)
- [API文档](./api-docs/)
- [设计稿](./design/)
- [测试报告](./test-reports/)

## 更新日志
- 2024-12-30: 初始架构设计完成
- 待更新...

---

*如有问题，请联系技术团队*