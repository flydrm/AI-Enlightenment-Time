# AI启蒙时光 - 开发指南

## 文档信息
- **版本**: 1.0
- **日期**: 2024-12-30
- **目标读者**: 开发团队
- **前置要求**: Android开发经验、Kotlin基础

## 目录
1. [开发环境搭建](#1-开发环境搭建)
2. [项目结构说明](#2-项目结构说明)
3. [编码规范](#3-编码规范)
4. [架构实现指南](#4-架构实现指南)
5. [UI开发指南](#5-ui开发指南)
6. [AI集成指南](#6-ai集成指南)
7. [数据存储指南](#7-数据存储指南)
8. [测试开发指南](#8-测试开发指南)
9. [性能优化指南](#9-性能优化指南)
10. [发布流程](#10-发布流程)

## 1. 开发环境搭建

### 1.1 必需工具
- **Android Studio**: Arctic Fox 2021.3.1 或更高版本
- **JDK**: 11 (推荐使用Android Studio自带)
- **Android SDK**: API 24-33
- **Git**: 2.30+

### 1.2 项目初始化
```bash
# 克隆项目
git clone https://github.com/your-org/AI-Enlightenment-Time.git
cd AI-Enlightenment-Time

# 安装Git hooks
./scripts/setup.sh

# 打开Android Studio
studio .
```

### 1.3 环境配置
在项目根目录创建 `local.properties`:
```properties
# SDK路径
sdk.dir=/Users/username/Library/Android/sdk

# API密钥（开发环境）
gemini.api.key=your_dev_key_here
gpt.api.key=your_dev_key_here
```

## 2. 项目结构说明

### 2.1 模块划分
```
app/
├── src/main/java/com/enlightenment/
│   ├── presentation/     # 表现层：UI、ViewModel
│   ├── domain/          # 领域层：业务逻辑、用例
│   ├── data/            # 数据层：仓库实现、数据源
│   ├── ai/              # AI服务：模型管理、调用策略
│   ├── multimedia/      # 多媒体：相机、音频、TTS
│   └── plugin/          # 插件系统：扩展功能
```

### 2.2 包命名规范
- **功能模块**: `com.enlightenment.feature.xxx`
- **通用组件**: `com.enlightenment.common.xxx`
- **工具类**: `com.enlightenment.utils.xxx`

## 3. 编码规范

### 3.1 Kotlin编码规范
```kotlin
// 文件命名：PascalCase
// HomeScreen.kt

// 类命名：PascalCase
class StoryViewModel : ViewModel() {
    // 属性命名：camelCase
    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()
    
    // 函数命名：camelCase
    fun generateStory(context: StoryContext) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            storyRepository.generateStory(context)
                .onSuccess { story ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            story = story
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }
}

// 常量命名：UPPER_SNAKE_CASE
companion object {
    private const val MAX_STORY_LENGTH = 300
    private const val DEFAULT_AGE = 4
}
```

### 3.2 注释规范
```kotlin
/**
 * 生成适合儿童的故事内容
 * 
 * @param age 儿童年龄，用于调整故事复杂度
 * @param interests 兴趣标签，用于个性化内容
 * @return 生成的故事结果
 */
suspend fun generateStory(
    age: Int = DEFAULT_AGE,
    interests: List<String> = emptyList()
): Result<Story>
```

### 3.3 代码组织原则
1. **单一职责**: 每个类只负责一个功能
2. **依赖倒置**: 依赖抽象而非具体实现
3. **接口隔离**: 使用小而专注的接口
4. **DRY原则**: 避免重复代码

## 4. 架构实现指南

### 4.1 Clean Architecture实现

#### Domain层实现
```kotlin
// Domain Model
data class Story(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val duration: Int, // 秒
    val questions: List<Question> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// Repository Interface
interface StoryRepository {
    suspend fun generateStory(context: StoryContext): Result<Story>
    suspend fun getStoryById(id: String): Story?
    suspend fun saveStory(story: Story)
    suspend fun getRecentStories(): List<Story>
}

// Use Case
class GenerateStoryUseCase @Inject constructor(
    private val repository: StoryRepository,
    private val analytics: AnalyticsService
) {
    suspend operator fun invoke(
        age: Int,
        interests: List<String>
    ): Result<Story> {
        analytics.logEvent("story_generation_started")
        
        return repository.generateStory(
            StoryContext(age = age, interests = interests)
        ).also { result ->
            if (result.isSuccess) {
                analytics.logEvent("story_generation_success")
            }
        }
    }
}
```

#### Data层实现
```kotlin
// Repository Implementation
class StoryRepositoryImpl @Inject constructor(
    private val localDataSource: StoryLocalDataSource,
    private val remoteDataSource: StoryRemoteDataSource,
    private val aiService: IAIService
) : StoryRepository {
    
    override suspend fun generateStory(
        context: StoryContext
    ): Result<Story> {
        // 先尝试从缓存获取
        localDataSource.getCachedStory(context)?.let {
            return Result.success(it)
        }
        
        // 调用AI服务生成
        return aiService.generateStory(context)
            .mapCatching { result ->
                val story = Story(
                    title = result.title,
                    content = result.content,
                    duration = calculateDuration(result.content),
                    questions = result.questions.map { it.toDomain() }
                )
                
                // 保存到本地
                localDataSource.saveStory(story)
                
                story
            }
    }
    
    private fun calculateDuration(content: String): Int {
        // 假设每分钟150字的朗读速度
        val words = content.length
        return (words * 60 / 150).coerceAtLeast(60)
    }
}
```

### 4.2 依赖注入配置
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "enlightenment.db"
        ).build()
    }
    
    @Provides
    fun provideStoryDao(database: AppDatabase): StoryDao {
        return database.storyDao()
    }
    
    @Provides
    @Singleton
    fun provideStoryRepository(
        localDataSource: StoryLocalDataSource,
        remoteDataSource: StoryRemoteDataSource,
        aiService: IAIService
    ): StoryRepository {
        return StoryRepositoryImpl(
            localDataSource,
            remoteDataSource,
            aiService
        )
    }
}
```

## 5. UI开发指南

### 5.1 Compose UI基础结构
```kotlin
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    HomeContent(
        uiState = uiState,
        onStoryClick = { navController.navigate("story") },
        onCameraClick = { navController.navigate("camera") },
        onVoiceClick = { navController.navigate("voice") }
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onStoryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    AdaptiveLayout { layoutConfig ->
        when (layoutConfig) {
            is LayoutConfig.PhonePortrait -> PhoneHomeLayout(
                uiState = uiState,
                onStoryClick = onStoryClick,
                onCameraClick = onCameraClick,
                onVoiceClick = onVoiceClick
            )
            is LayoutConfig.TabletLandscape -> TabletHomeLayout(
                uiState = uiState,
                onStoryClick = onStoryClick,
                onCameraClick = onCameraClick,
                onVoiceClick = onVoiceClick
            )
            // 其他布局配置...
        }
    }
}
```

### 5.2 主题实现
```kotlin
// 色彩定义
private val LightColorScheme = lightColorScheme(
    primary = WarmThemeColors.PrimaryRed,
    onPrimary = Color.White,
    secondary = WarmThemeColors.SkyBlue,
    background = WarmThemeColors.CreamBg,
    surface = Color.White,
    onSurface = WarmThemeColors.WoodBrown
)

// 主题组合
@Composable
fun EnlightenmentTheme(
    darkTheme: Boolean = false, // 儿童应用通常不需要深色模式
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChildFriendlyTypography,
        shapes = RoundedShapes,
        content = content
    )
}
```

### 5.3 动画实现
```kotlin
@Composable
fun RedPandaAnimation(
    mood: PandaMood,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // 呼吸动画
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // 摇摆动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .rotate(rotation)
    ) {
        Image(
            painter = painterResource(
                id = getPandaResource(mood)
            ),
            contentDescription = "红色小熊猫"
        )
    }
}
```

## 6. AI集成指南

### 6.1 AI服务配置
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    
    @Provides
    @Singleton
    fun provideGeminiService(
        @ApplicationContext context: Context
    ): GeminiService {
        val apiKey = context.getSecureString("gemini_api_key")
        
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $apiKey")
                            .build()
                        chain.proceed(request)
                    }
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .build()
            .create(GeminiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAIServiceManager(
        geminiService: GeminiService,
        gptService: GPTService,
        modelSelector: ModelSelector,
        healthMonitor: ModelHealthMonitor
    ): IAIService {
        return AIServiceManager(
            geminiService = geminiService,
            gptService = gptService,
            modelSelector = modelSelector,
            healthMonitor = healthMonitor
        )
    }
}
```

### 6.2 模型降级实现
```kotlin
class AIServiceManager(
    private val geminiService: GeminiService,
    private val gptService: GPTService,
    private val modelSelector: ModelSelector,
    private val healthMonitor: ModelHealthMonitor
) : IAIService {
    
    override suspend fun generateStory(
        context: StoryContext
    ): Result<StoryResult> {
        val capability = AICapability.TEXT_GENERATION
        
        // 选择主模型
        val primaryModel = modelSelector.selectPrimaryModel(capability)
        
        return try {
            // 尝试主模型
            callModel(primaryModel, context).also {
                healthMonitor.recordSuccess(primaryModel)
            }
        } catch (e: Exception) {
            healthMonitor.recordFailure(primaryModel, e)
            
            // 尝试备用模型
            val fallbackModel = modelSelector.selectFallbackModel(
                failedModel = primaryModel,
                capability = capability
            )
            
            if (fallbackModel != null) {
                try {
                    callModel(fallbackModel, context).also {
                        healthMonitor.recordSuccess(fallbackModel)
                    }
                } catch (e2: Exception) {
                    healthMonitor.recordFailure(fallbackModel, e2)
                    // 使用本地模板
                    Result.success(generateLocalStory(context))
                }
            } else {
                // 直接使用本地模板
                Result.success(generateLocalStory(context))
            }
        }
    }
    
    private suspend fun callModel(
        model: AIModel,
        context: StoryContext
    ): Result<StoryResult> {
        return when (model.type) {
            ModelType.GEMINI -> geminiService.generateStory(
                context.toGeminiRequest()
            )
            ModelType.GPT -> gptService.generateStory(
                context.toGPTRequest()
            )
            else -> throw UnsupportedOperationException()
        }
    }
    
    private fun generateLocalStory(
        context: StoryContext
    ): StoryResult {
        // 使用预定义模板生成故事
        val template = StoryTemplates.getTemplate(context.age)
        return StoryResult(
            title = template.title,
            content = template.content.format(context.interests.firstOrNull() ?: "探险"),
            questions = template.questions
        )
    }
}
```

## 7. 数据存储指南

### 7.1 Room数据库实现
```kotlin
// Entity定义
@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val duration: Int,
    val questions: String, // JSON
    val createdAt: Long,
    val lastPlayedAt: Long? = null,
    val playCount: Int = 0
)

// DAO定义
@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentStories(limit: Int = 10): List<StoryEntity>
    
    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getStoryById(id: String): StoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)
    
    @Query("UPDATE stories SET lastPlayedAt = :timestamp, playCount = playCount + 1 WHERE id = :id")
    suspend fun updatePlayInfo(id: String, timestamp: Long)
    
    @Query("DELETE FROM stories WHERE createdAt < :threshold")
    suspend fun deleteOldStories(threshold: Long)
}

// 类型转换器
@ProvidedTypeConverter
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromQuestionList(questions: List<Question>): String {
        return gson.toJson(questions)
    }
    
    @TypeConverter
    fun toQuestionList(json: String): List<Question> {
        val type = object : TypeToken<List<Question>>() {}.type
        return gson.fromJson(json, type)
    }
}
```

### 7.2 安全存储实现
```kotlin
// 密钥管理器
class KeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyAlias = "EnlightenmentKeyAlias"
    private val preferences = context.getSharedPreferences(
        "secure_prefs",
        Context.MODE_PRIVATE
    )
    
    init {
        generateKey()
    }
    
    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    fun saveApiKey(service: String, apiKey: String) {
        val encrypted = encrypt(apiKey)
        preferences.edit()
            .putString("${service}_key", encrypted)
            .apply()
    }
    
    fun getApiKey(service: String): String? {
        val encrypted = preferences.getString("${service}_key", null)
        return encrypted?.let { decrypt(it) }
    }
    
    private fun encrypt(plainText: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryption = cipher.doFinal(plainText.toByteArray())
        
        return Base64.encodeToString(iv + encryption, Base64.DEFAULT)
    }
    
    private fun decrypt(encryptedText: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        val encrypted = Base64.decode(encryptedText, Base64.DEFAULT)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, encrypted, 0, 12)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        return String(
            cipher.doFinal(encrypted, 12, encrypted.size - 12)
        )
    }
}
```

## 8. 测试开发指南

### 8.1 单元测试编写
```kotlin
@ExperimentalCoroutinesApi
class StoryViewModelTest {
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @MockK
    private lateinit var generateStoryUseCase: GenerateStoryUseCase
    
    @MockK
    private lateinit var analyticsService: AnalyticsService
    
    private lateinit var viewModel: StoryViewModel
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = StoryViewModel(
            generateStoryUseCase = generateStoryUseCase,
            analyticsService = analyticsService
        )
    }
    
    @Test
    fun `generateStory success updates ui state correctly`() = runTest {
        // Given
        val expectedStory = TestDataFactory.createTestStory()
        coEvery { 
            generateStoryUseCase(any(), any()) 
        } returns Result.success(expectedStory)
        
        // When
        viewModel.generateStory(age = 4, interests = listOf("太空"))
        
        // Then
        viewModel.uiState.test {
            // 初始状态
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertNull(initialState.story)
            
            // 加载状态
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            
            // 成功状态
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(expectedStory, successState.story)
            assertNull(successState.error)
        }
        
        // 验证分析事件
        verify { analyticsService.logEvent("story_generated") }
    }
}
```

### 8.2 UI测试编写
```kotlin
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Before
    fun setup() {
        // 设置测试环境
        hiltRule.inject()
        
        composeTestRule.activity.setContent {
            EnlightenmentTheme {
                HomeScreen(
                    navController = TestNavHostController(
                        composeTestRule.activity
                    )
                )
            }
        }
    }
    
    @Test
    fun redPandaGreeting_displaysCorrectly() {
        // 验证小熊猫显示
        composeTestRule
            .onNodeWithTag("red_panda_avatar")
            .assertIsDisplayed()
            .assertHasNoClickAction() // 头像不可点击
        
        // 验证问候语
        composeTestRule
            .onNodeWithText("嗨，小朋友！")
            .assertIsDisplayed()
        
        // 验证语音波形动画
        composeTestRule
            .onNodeWithTag("voice_wave_animation")
            .assertExists()
    }
    
    @Test
    fun featureCards_navigateCorrectly() {
        // 测试故事卡片导航
        composeTestRule
            .onNodeWithText("今日故事")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        
        // 验证导航到故事页面
        composeTestRule
            .onNodeWithTag("story_screen")
            .assertIsDisplayed()
    }
    
    @Test
    fun responsiveLayout_adaptsToScreenSize() {
        // 模拟平板尺寸
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalConfiguration provides Configuration().apply {
                    screenWidthDp = 768
                    orientation = Configuration.ORIENTATION_LANDSCAPE
                }
            ) {
                EnlightenmentTheme {
                    HomeScreen(rememberNavController())
                }
            }
        }
        
        // 验证网格布局
        composeTestRule
            .onAllNodesWithTag("feature_card")
            .assertCountEquals(6) // 平板显示更多内容
    }
}
```

## 9. 性能优化指南

### 9.1 启动优化
```kotlin
class EnlightenmentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 使用协程进行异步初始化
        GlobalScope.launch(Dispatchers.Default) {
            initializeNonCriticalComponents()
        }
        
        // 只初始化关键组件
        initializeCriticalComponents()
    }
    
    private fun initializeCriticalComponents() {
        // Hilt初始化（必需）
        // Timber初始化（用于日志）
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
    
    private suspend fun initializeNonCriticalComponents() {
        // 延迟初始化非关键组件
        withContext(Dispatchers.IO) {
            // 预加载字体
            preloadFonts()
            
            // 预热图片加载器
            warmupImageLoader()
            
            // 初始化分析服务
            initializeAnalytics()
        }
    }
}
```

### 9.2 内存优化
```kotlin
// 图片加载优化
@Composable
fun OptimizedImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 使用25%的可用内存
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .scale(Scale.FILL)
            .size(ViewSizeResolver.invoke())
            .build(),
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier
    )
}
```

### 9.3 渲染优化
```kotlin
// 使用remember避免重复计算
@Composable
fun StoryCard(
    story: Story,
    onClick: () -> Unit
) {
    // 缓存计算结果
    val readingTime = remember(story.content) {
        calculateReadingTime(story.content)
    }
    
    // 使用derivedStateOf优化状态派生
    val isNew = remember {
        derivedStateOf {
            System.currentTimeMillis() - story.createdAt < 24 * 60 * 60 * 1000
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = story.title,
                style = Typography.h2
            )
            
            if (isNew.value) {
                Badge(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("新")
                }
            }
            
            Text(
                text = "阅读时间：${readingTime}分钟",
                style = Typography.body2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

## 10. 发布流程

### 10.1 版本管理
```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
        
        // 版本命名规范：major.minor.patch
        // major: 重大更新
        // minor: 功能更新
        // patch: 问题修复
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // 签名配置
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 10.2 发布检查清单
```markdown
## 发布前检查清单

### 代码检查
- [ ] 所有测试通过
- [ ] 代码覆盖率达标 (>80%)
- [ ] 无编译警告
- [ ] API密钥已更换为生产环境

### 功能检查
- [ ] 核心功能正常
- [ ] AI降级策略工作正常
- [ ] 家长控制功能正常
- [ ] 无内存泄漏

### 性能检查
- [ ] 启动时间 < 3秒
- [ ] 内存占用 < 150MB
- [ ] 包大小 < 50MB

### 安全检查
- [ ] ProGuard规则完整
- [ ] 敏感信息已混淆
- [ ] 权限申请合理

### 文档更新
- [ ] 版本更新日志
- [ ] API文档更新
- [ ] 用户指南更新
```

### 10.3 发布脚本
```bash
#!/bin/bash
# scripts/release.sh

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "Usage: ./release.sh <version>"
    exit 1
fi

echo "Building release version $VERSION..."

# 清理
./gradlew clean

# 运行测试
./gradlew test
if [ $? -ne 0 ]; then
    echo "Tests failed!"
    exit 1
fi

# 构建发布版本
./gradlew assembleRelease

# 生成mapping文件备份
cp app/build/outputs/mapping/release/mapping.txt \
   releases/mapping-$VERSION.txt

# 生成APK
cp app/build/outputs/apk/release/app-release.apk \
   releases/enlightenment-$VERSION.apk

echo "Release $VERSION built successfully!"
echo "APK: releases/enlightenment-$VERSION.apk"
echo "Mapping: releases/mapping-$VERSION.txt"
```

---

*本指南是活文档，将随项目发展持续更新*