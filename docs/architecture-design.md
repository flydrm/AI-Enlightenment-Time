# AI启蒙时光 - 系统架构设计文档

## 文档信息
- **版本**: 1.0
- **日期**: 2024-12-30
- **作者**: 架构设计团队
- **状态**: 已确认

## 目录
1. [项目概述](#1-项目概述)
2. [技术架构](#2-技术架构)
3. [UI设计方案](#3-ui设计方案)
4. [测试架构](#4-测试架构)
5. [项目结构](#5-项目结构)
6. [核心模块设计](#6-核心模块设计)
7. [开发规范](#7-开发规范)
8. [部署架构](#8-部署架构)
9. [性能指标](#9-性能指标)
10. [开发路线图](#10-开发路线图)

## 1. 项目概述

### 1.1 项目定位
AI启蒙时光是一款面向3-6岁儿童的Android教育应用，通过AI技术提供每日15分钟的互动学习体验。

### 1.2 核心特性
- **AI驱动**: 集成多个AI模型，提供智能化的学习内容
- **儿童友好**: 专为儿童设计的交互界面和体验
- **隐私安全**: 离线优先，严格的数据保护
- **响应式设计**: 支持手机和平板设备

### 1.3 目标用户
- 主要用户：3-6岁儿童（特别是小男孩）
- 决策用户：家长
- 使用场景：每日早晚15分钟的学习时光

## 2. 技术架构

### 2.1 架构选型
采用**原生Android Clean Architecture**方案，基于MVVM模式。

### 2.2 架构分层

```
┌────────────────────────────────────────────────────────────┐
│                    AI启蒙时光应用架构                        │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                  Presentation Layer                   │ │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────┐  │ │
│  │  │  Compose   │ │ ViewModels │ │ Navigation     │  │ │
│  │  │  UI        │ │   (MVVM)   │ │ Component      │  │ │
│  │  └────────────┘ └────────────┘ └────────────────┘  │ │
│  └──────────────────────────────────────────────────────┘ │
│                             ↕                              │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                    Domain Layer                       │ │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────┐  │ │
│  │  │ Use Cases  │ │   Models   │ │ Repository     │  │ │
│  │  │            │ │            │ │ Interfaces     │  │ │
│  │  └────────────┘ └────────────┘ └────────────────┘  │ │
│  └──────────────────────────────────────────────────────┘ │
│                             ↕                              │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                     Data Layer                        │ │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────┐  │ │
│  │  │    Room    │ │ Retrofit   │ │ DataStore      │  │ │
│  │  │    DB      │ │ API Client │ │ Preferences    │  │ │
│  │  └────────────┘ └────────────┘ └────────────────┘  │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

### 2.3 技术栈

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| **语言** | Kotlin | 现代化、类型安全、协程支持 |
| **UI框架** | Jetpack Compose | 声明式UI、动画丰富 |
| **架构组件** | ViewModel, LiveData, Navigation | Google官方推荐 |
| **依赖注入** | Hilt | 编译时依赖注入 |
| **数据库** | Room | SQLite的抽象层 |
| **网络** | Retrofit + OkHttp | 成熟的网络框架 |
| **异步** | Kotlin Coroutines + Flow | 结构化并发 |
| **多媒体** | CameraX, ExoPlayer | 相机和媒体播放 |

## 3. UI设计方案

### 3.1 设计风格
采用**温暖童趣风格**设计方案：
- 主题色：红色系（活力红 #E53935）
- 吉祥物：红色小熊猫
- 视觉风格：圆润可爱、手绘质感

### 3.2 色彩系统

```kotlin
object WarmThemeColors {
    val PrimaryRed = Color(0xFFE53935)      // 主红色
    val SoftRed = Color(0xFFEF5350)         // 柔和红
    val CreamBg = Color(0xFFFFF8E1)         // 奶油背景
    val SkyBlue = Color(0xFF87CEEB)        // 天空蓝
    val GrassGreen = Color(0xFF8BC34A)     // 草地绿
    val SunYellow = Color(0xFFFFEB3B)      // 阳光黄
    val WoodBrown = Color(0xFF795548)      // 木纹棕
}
```

### 3.3 响应式设计

#### 设备断点
```kotlin
object DeviceBreakpoints {
    const val PHONE_SMALL = 320    // dp - 小手机
    const val PHONE_NORMAL = 360   // dp - 标准手机
    const val PHONE_LARGE = 400    // dp - 大手机
    const val TABLET_SMALL = 600   // dp - 7寸平板
    const val TABLET_NORMAL = 768  // dp - 10寸平板
    const val TABLET_LARGE = 900   // dp - 12寸+平板
}
```

#### 布局策略
- **手机竖屏**: 单列卡片式布局
- **手机横屏**: 双列布局
- **平板竖屏**: 2列网格布局
- **平板横屏**: 3栏分屏布局

### 3.4 交互设计原则
1. **大触控目标**: 最小64dp（手机）/ 72dp（平板）
2. **语音优先**: 所有内容支持语音播报
3. **即时反馈**: 点击响应<100ms
4. **动画引导**: 使用动画引导注意力
5. **防误触**: 重要操作需二次确认

## 4. 测试架构

### 4.1 测试金字塔

```
                    ┌─────────────┐
                   │   E2E测试    │ 10%
                  │  端到端场景   │
                 ├───────────────┤
                │  集成测试      │ 20%
               │  API/组件集成   │
              ├─────────────────┤
             │    功能测试      │ 30%
            │   UI/交互测试     │
           ├───────────────────┤
          │     单元测试        │ 40%
         │  业务逻辑/工具类     │
        └───────────────────────┘
```

### 4.2 测试策略

| 测试类型 | 覆盖范围 | 工具 | 目标 |
|---------|---------|------|------|
| **单元测试** | ViewModel, UseCase, Repository | JUnit, Mockito | 覆盖率>80% |
| **UI测试** | Compose组件, 用户交互 | Compose Testing | 核心流程100% |
| **集成测试** | API调用, 数据库操作 | MockWebServer | 关键路径覆盖 |
| **E2E测试** | 完整用户旅程 | Espresso | 15分钟体验流程 |

### 4.3 专项测试

1. **性能测试**
   - 启动时间 < 3秒
   - 内存占用 < 150MB
   - 帧率 > 30fps

2. **兼容性测试**
   - Android 7.0+ (API 24+)
   - 多种屏幕尺寸
   - 不同内存配置

3. **安全测试**
   - API密钥加密验证
   - 隐私数据保护
   - 家长控制功能

4. **儿童适用性测试**
   - 触控目标大小
   - 色彩对比度
   - 语音引导完整性

## 5. 项目结构

```
AI-Enlightenment-Time/
├── app/                                    # 主应用模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/enlightenment/
│   │   │   │   ├── EnlightenmentApp.kt   # Application类
│   │   │   │   ├── MainActivity.kt        # 单Activity架构
│   │   │   │   ├── presentation/         # 表现层
│   │   │   │   ├── domain/              # 领域层
│   │   │   │   ├── data/                # 数据层
│   │   │   │   ├── ai/                  # AI服务模块
│   │   │   │   ├── multimedia/          # 多媒体处理
│   │   │   │   └── plugin/              # 插件系统
│   │   │   └── res/                     # 资源文件
│   │   ├── test/                        # 单元测试
│   │   ├── androidTest/                 # 仪器化测试
│   │   └── sharedTest/                  # 共享测试代码
│   └── build.gradle.kts
├── buildSrc/                            # 构建逻辑
├── gradle/
├── .github/                             # CI/CD配置
├── docs/                                # 文档
├── scripts/                             # 脚本
└── README.md
```

## 6. 核心模块设计

### 6.1 AI服务模块

#### 接口定义
```kotlin
interface IAIService {
    suspend fun generateStory(context: StoryContext): Result<StoryResult>
    suspend fun generateDialogue(context: DialogueContext): Result<DialogueResult>
    suspend fun embedImage(image: ImageBlob): Result<Vector>
    suspend fun rerank(candidates: List<Candidate>): Result<List<Candidate>>
    suspend fun generateImage(spec: ImageSpec): Result<ImageResult>
}
```

#### 模型管理
- **主模型池**: GEMINI-2.5-PRO, GPT-5-PRO
- **嵌入模型**: Qwen3-Embedding-8B
- **重排模型**: BAAI/bge-reranker-v2-m3
- **图像生成**: grok-4-imageGen

#### 降级策略
1. 主模型调用失败 → 尝试备用模型
2. 备用模型失败 → 使用本地缓存
3. 无缓存可用 → 显示友好提示

### 6.2 数据存储设计

#### Room数据库
```kotlin
@Database(
    entities = [
        StoryEntity::class,
        AchievementEntity::class,
        ProgressEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun achievementDao(): AchievementDao
    abstract fun progressDao(): ProgressDao
}
```

#### 安全存储
- API密钥: Android Keystore加密
- 用户数据: DataStore加密存储
- 缓存策略: LRU缓存 + 持久化

### 6.3 插件系统

```kotlin
interface IPlugin {
    val id: String
    val version: String
    fun initialize(context: PluginContext)
    fun execute(params: Map<String, Any>): Result<Any>
}

class PluginManager {
    fun loadPlugin(pluginPath: String): Result<IPlugin>
    fun unloadPlugin(pluginId: String)
    fun executePlugin(pluginId: String, params: Map<String, Any>): Result<Any>
}
```

## 7. 开发规范

### 7.1 代码规范
- 遵循Kotlin官方编码规范
- 使用KtLint进行代码格式化
- 函数长度不超过30行
- 类不超过300行

### 7.2 命名规范
- **Activity/Fragment**: `XxxActivity`, `XxxFragment`
- **ViewModel**: `XxxViewModel`
- **UseCase**: `XxxUseCase`
- **Repository**: `XxxRepository`

### 7.3 Git规范
- 分支策略: Git Flow
- 提交信息: `type(scope): message`
- PR需要至少1人review

## 8. 部署架构

### 8.1 构建配置
```kotlin
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
        }
        create("prod") {
            dimension = "environment"
        }
    }
}
```

### 8.2 发布流程
1. 开发环境测试
2. 测试环境验证
3. 生产环境灰度发布
4. 全量发布

## 9. 性能指标

### 9.1 性能目标
| 指标 | 目标值 | 测量方法 |
|------|--------|----------|
| 冷启动时间 | < 3秒 | Firebase Performance |
| 内存占用 | < 150MB | Memory Profiler |
| 帧率 | > 30fps | GPU Profiler |
| 电池消耗 | < 5%/小时 | Battery Historian |
| ANR率 | < 0.05% | Firebase Crashlytics |
| 崩溃率 | < 0.1% | Firebase Crashlytics |

### 9.2 优化策略
1. **启动优化**: 懒加载、预加载关键资源
2. **内存优化**: 图片压缩、及时释放资源
3. **渲染优化**: 减少过度绘制、使用硬件加速
4. **网络优化**: 请求合并、缓存策略

## 10. 开发路线图

### Phase 1: 基础架构搭建（2周）
- [x] 项目初始化和基础配置
- [ ] Clean Architecture层级搭建
- [ ] 依赖注入框架集成
- [ ] 基础UI主题实现

### Phase 2: 核心功能开发（4周）
- [ ] AI服务集成和降级策略
- [ ] 主界面和导航实现
- [ ] 故事生成和播放功能
- [ ] 语音交互基础功能

### Phase 3: 响应式UI实现（2周）
- [ ] 手机/平板自适应布局
- [ ] 动画和交互效果
- [ ] 无障碍功能支持

### Phase 4: 多媒体功能（2周）
- [ ] 相机功能集成
- [ ] 图片分析功能
- [ ] TTS语音合成

### Phase 5: 测试和优化（3周）
- [ ] 单元测试编写（覆盖率>80%）
- [ ] UI自动化测试
- [ ] 性能优化
- [ ] 安全加固

### Phase 6: 发布准备（1周）
- [ ] 最终测试
- [ ] 文档完善
- [ ] 发布配置

## 附录

### A. 相关文档
- [需求文档](../README.md)
- [API设计文档](./api-design.md)
- [测试计划](./test-plan.md)

### B. 参考资料
- [Android开发文档](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

*本文档会随着项目进展持续更新*