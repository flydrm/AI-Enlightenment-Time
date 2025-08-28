# AI启蒙时光 - 测试策略文档

## 文档信息
- **版本**: 1.0
- **日期**: 2024-12-30
- **负责人**: 测试团队
- **状态**: 已确认

## 目录
1. [测试目标](#1-测试目标)
2. [测试范围](#2-测试范围)
3. [测试策略](#3-测试策略)
4. [测试类型](#4-测试类型)
5. [测试环境](#5-测试环境)
6. [测试工具](#6-测试工具)
7. [测试用例设计](#7-测试用例设计)
8. [自动化策略](#8-自动化策略)
9. [测试指标](#9-测试指标)
10. [风险管理](#10-风险管理)

## 1. 测试目标

### 1.1 主要目标
- 确保应用功能正确性和稳定性
- 验证AI功能的准确性和响应时间
- 保证儿童使用的安全性和易用性
- 验证多设备兼容性和响应式设计
- 确保数据隐私和安全保护

### 1.2 质量目标
| 指标 | 目标值 | 优先级 |
|------|--------|--------|
| 代码覆盖率 | >80% | 高 |
| UI测试覆盖率 | >70% | 高 |
| 崩溃率 | <0.1% | 关键 |
| ANR率 | <0.05% | 关键 |
| 性能测试通过率 | 100% | 高 |
| 安全测试通过率 | 100% | 关键 |

## 2. 测试范围

### 2.1 功能测试范围
- **核心功能**
  - 每日15分钟学习流程
  - AI故事生成和播放
  - 语音交互功能
  - 拍照识别功能
  - 成就系统
  
- **辅助功能**
  - 用户注册和登录
  - 家长控制面板
  - 设置功能
  - 数据同步

### 2.2 非功能测试范围
- 性能测试
- 兼容性测试
- 安全测试
- 可用性测试
- 无障碍测试

## 3. 测试策略

### 3.1 测试金字塔
```
         ┌─────────────┐
        │   E2E测试    │ 10%
       │  关键用户旅程  │
      ├───────────────┤
     │   集成测试     │ 20%
    │  模块间交互     │
   ├─────────────────┤
  │    功能测试      │ 30%
 │   UI和业务逻辑    │
├───────────────────┤
│     单元测试       │ 40%
│   类和方法级别     │
└───────────────────┘
```

### 3.2 测试方法
- **左移测试**: 开发阶段即开始测试
- **持续测试**: CI/CD pipeline集成
- **风险驱动**: 优先测试高风险功能
- **自动化优先**: 尽可能自动化

## 4. 测试类型

### 4.1 单元测试

#### 测试范围
```kotlin
// ViewModel测试示例
@ExperimentalCoroutinesApi
class StoryViewModelTest {
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var storyRepository: StoryRepository
    
    private lateinit var viewModel: StoryViewModel
    
    @Test
    fun `generateStory should emit loading and success states`() = runTest {
        // Given
        val expectedStory = TestDataFactory.createTestStory()
        whenever(storyRepository.generateStory(any()))
            .thenReturn(Result.success(expectedStory))
        
        // When
        viewModel.generateStory()
        
        // Then
        viewModel.uiState.test {
            assertEquals(StoryUiState.Loading, awaitItem())
            assertEquals(StoryUiState.Success(expectedStory), awaitItem())
        }
    }
    
    @Test
    fun `generateStory should handle network error`() = runTest {
        // Given
        whenever(storyRepository.generateStory(any()))
            .thenReturn(Result.failure(NetworkException()))
        
        // When
        viewModel.generateStory()
        
        // Then
        viewModel.uiState.test {
            assertEquals(StoryUiState.Loading, awaitItem())
            assertTrue(awaitItem() is StoryUiState.Error)
        }
    }
}
```

### 4.2 UI测试

#### Compose UI测试
```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Before
    fun setup() {
        // 设置测试环境
        composeTestRule.activity.setContent {
            EnlightenmentTheme {
                HomeScreen()
            }
        }
    }
    
    @Test
    fun redPandaAvatar_isDisplayed() {
        composeTestRule
            .onNodeWithTag("red_panda_avatar")
            .assertIsDisplayed()
    }
    
    @Test
    fun storyCard_clickNavigatesToStoryScreen() {
        // 点击故事卡片
        composeTestRule
            .onNodeWithText("今日故事")
            .performClick()
        
        // 验证导航到故事界面
        composeTestRule
            .onNodeWithTag("story_screen")
            .assertExists()
    }
    
    @Test
    fun touchTargetSize_meetsAccessibilityRequirements() {
        // 验证所有可点击元素满足最小尺寸要求
        composeTestRule
            .onAllNodes(hasClickAction())
            .assertAll(hasMinimumSize(64.dp))
    }
}
```

### 4.3 集成测试

#### API集成测试
```kotlin
@RunWith(AndroidJUnit4::class)
class AIServiceIntegrationTest {
    @get:Rule
    val mockWebServerRule = MockWebServerRule()
    
    private lateinit var aiService: AIService
    
    @Before
    fun setup() {
        aiService = AIService(
            baseUrl = mockWebServerRule.baseUrl,
            apiKey = "test_key"
        )
    }
    
    @Test
    fun testStoryGeneration_withPrimaryModel() = runTest {
        // 模拟成功响应
        mockWebServerRule.enqueue(
            MockResponse()
                .setBody("""
                    {
                        "story": {
                            "title": "月球探险",
                            "content": "小明和红色小熊猫一起...",
                            "duration": 180
                        }
                    }
                """)
                .setResponseCode(200)
        )
        
        val result = aiService.generateStory(StoryContext(age = 4))
        
        assertTrue(result.isSuccess)
        assertEquals("月球探险", result.getOrNull()?.title)
    }
    
    @Test
    fun testModelFallback_whenPrimaryFails() = runTest {
        // 主模型失败
        mockWebServerRule.enqueue(
            MockResponse().setResponseCode(500)
        )
        
        // 备用模型成功
        mockWebServerRule.enqueue(
            MockResponse()
                .setBody("""{"story": {"title": "备用故事"}}""")
                .setResponseCode(200)
        )
        
        val result = aiService.generateStory(StoryContext())
        
        assertTrue(result.isSuccess)
        assertEquals("备用故事", result.getOrNull()?.title)
    }
}
```

### 4.4 E2E测试

#### 完整用户旅程测试
```kotlin
@LargeTest
@RunWith(AndroidJUnit4::class)
class DailyLearningE2ETest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun complete15MinuteLearningSession() {
        val startTime = System.currentTimeMillis()
        
        // 1. 应用启动
        onView(withId(R.id.splash_screen))
            .check(matches(isDisplayed()))
        
        Thread.sleep(2000)
        
        // 2. 主界面显示
        onView(withText("嗨，小朋友！"))
            .check(matches(isDisplayed()))
        
        // 3. 开始今日故事
        onView(withText("今日故事"))
            .perform(click())
        
        // 4. 等待故事加载
        onView(withId(R.id.story_content))
            .check(matches(isDisplayed()))
        
        // 5. 播放故事（模拟3分钟）
        onView(withId(R.id.play_button))
            .perform(click())
        
        Thread.sleep(3000) // 实际测试中应等待完整播放
        
        // 6. 回答互动问题
        onView(withText("小熊猫是什么颜色的？"))
            .check(matches(isDisplayed()))
        
        onView(withText("红色"))
            .perform(click())
        
        // 7. 语音交互测试
        onView(withId(R.id.voice_button))
            .perform(click())
        
        // 模拟语音输入
        InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand("input text '我喜欢小熊猫'")
        
        // 8. 拍照功能
        onView(withText("拍照探索"))
            .perform(click())
        
        // 模拟拍照
        onView(withId(R.id.capture_button))
            .perform(click())
        
        // 9. 查看成就
        onView(withText("获得新成就！"))
            .check(matches(isDisplayed()))
        
        // 10. 验证总时长
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertTrue("Session should complete within 15 minutes", 
            duration <= 15 * 60 * 1000)
    }
}
```

### 4.5 性能测试

#### 启动性能测试
```kotlin
@RunWith(AndroidJUnit4::class)
class StartupPerformanceTest {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    
    @Test
    fun measureColdStartup() {
        benchmarkRule.measureRepeated(
            packageName = "com.enlightenment",
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()
        }
        
        // 验证冷启动时间 < 3秒
        val medianStartupTime = benchmarkRule.getMedianMetric("timeToInitialDisplay")
        assertTrue(medianStartupTime < 3000)
    }
    
    @Test
    fun measureMemoryUsage() {
        benchmarkRule.measureRepeated(
            packageName = "com.enlightenment",
            metrics = listOf(MemoryUsageMetric()),
            iterations = 3
        ) {
            startActivityAndWait()
            
            // 执行典型用户操作
            device.findObject(By.text("今日故事")).click()
            device.wait(Until.hasObject(By.text("开始")), 5000)
            device.findObject(By.text("开始")).click()
        }
        
        // 验证内存使用 < 150MB
        val peakMemory = benchmarkRule.getMaxMetric("memoryUsage")
        assertTrue(peakMemory < 150 * 1024 * 1024)
    }
}
```

### 4.6 安全测试

#### 数据加密测试
```kotlin
@RunWith(AndroidJUnit4::class)
class SecurityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testApiKeyEncryption() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val keyManager = KeyManager(context)
        
        // 保存测试密钥
        val testKey = "test_api_key_12345"
        keyManager.saveApiKey("gemini", testKey)
        
        // 验证SharedPreferences中没有明文
        val prefs = context.getSharedPreferences("keys", Context.MODE_PRIVATE)
        val storedValue = prefs.getString("gemini_key", null)
        
        assertNotNull(storedValue)
        assertNotEquals(testKey, storedValue)
        assertTrue(storedValue!!.startsWith("encrypted_"))
        
        // 验证解密后的值正确
        val decryptedKey = keyManager.getApiKey("gemini")
        assertEquals(testKey, decryptedKey)
    }
    
    @Test
    fun testPrivacyCompliance() {
        // 模拟未授权状态
        val privacyManager = PrivacyManager(
            ApplicationProvider.getApplicationContext()
        )
        privacyManager.setParentConsent(false)
        
        // 尝试上传图片
        onView(withText("拍照探索"))
            .perform(click())
        
        onView(withId(R.id.capture_button))
            .perform(click())
        
        // 验证显示授权提示
        onView(withText("需要家长授权"))
            .check(matches(isDisplayed()))
        
        // 验证没有网络请求发出
        // (通过网络拦截器验证)
    }
}
```

## 5. 测试环境

### 5.1 设备矩阵
| 设备类型 | API级别 | 屏幕尺寸 | 内存 | 测试重点 |
|---------|---------|---------|------|---------|
| 小屏手机 | 24, 26 | 320dp | 2GB | 性能、布局 |
| 标准手机 | 28, 30, 33 | 360dp | 4GB | 全功能 |
| 大屏手机 | 30, 31, 33 | 400dp | 6GB | 全功能 |
| 7寸平板 | 26, 28, 30 | 600dp | 3GB | 响应式布局 |
| 10寸平板 | 28, 30, 33 | 768dp | 4GB | 响应式布局 |

### 5.2 测试环境配置
```yaml
# test-config.yml
environments:
  development:
    api_base_url: "https://dev-api.enlightenment.com"
    use_mock_data: true
    log_level: DEBUG
    
  staging:
    api_base_url: "https://staging-api.enlightenment.com"
    use_mock_data: false
    log_level: INFO
    
  production:
    api_base_url: "https://api.enlightenment.com"
    use_mock_data: false
    log_level: ERROR
```

## 6. 测试工具

### 6.1 工具清单
| 工具 | 用途 | 版本 |
|------|------|------|
| JUnit | 单元测试框架 | 4.13.2 |
| Mockito | Mock框架 | 4.6.1 |
| Espresso | UI测试 | 3.5.1 |
| Compose Testing | Compose UI测试 | 1.4.3 |
| MockWebServer | API Mock | 4.11.0 |
| Firebase Test Lab | 云测试 | Latest |
| LeakCanary | 内存泄漏检测 | 2.12 |

### 6.2 CI/CD集成
```yaml
# .github/workflows/test.yml
name: Test Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      
      - name: Run Unit Tests
        run: ./gradlew test
      
      - name: Generate Coverage Report
        run: ./gradlew jacocoTestReport
      
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./app/build/reports/jacoco/test/jacocoTestReport.xml
          
  instrumented-test:
    runs-on: macos-latest
    strategy:
      matrix:
        api-level: [26, 30, 33]
        
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Instrumented Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          target: google_apis
          arch: x86_64
          script: ./gradlew connectedAndroidTest
```

## 7. 测试用例设计

### 7.1 测试用例模板
```kotlin
/**
 * 测试用例ID: TC_HOME_001
 * 测试目标: 验证主页加载和显示
 * 前置条件: 用户已登录
 * 测试步骤:
 * 1. 启动应用
 * 2. 等待主页加载
 * 预期结果:
 * - 显示红色小熊猫
 * - 显示欢迎语
 * - 显示功能卡片
 */
@Test
fun testHomePageDisplay() {
    // 实现测试逻辑
}
```

### 7.2 关键测试场景

#### 儿童安全测试场景
1. 防沉迷测试
2. 内容过滤测试
3. 家长控制测试
4. 隐私保护测试

#### AI功能测试场景
1. 故事生成质量
2. 语音识别准确性
3. 图像识别准确性
4. 响应时间测试

## 8. 自动化策略

### 8.1 自动化原则
- 优先自动化高频执行的测试
- 自动化稳定的测试场景
- 保持测试的可维护性
- 定期review和更新测试

### 8.2 自动化覆盖目标
| 测试类型 | 自动化比例 | 说明 |
|---------|-----------|------|
| 单元测试 | 100% | 全自动化 |
| API测试 | 100% | 全自动化 |
| UI测试 | 80% | 核心流程自动化 |
| E2E测试 | 60% | 关键路径自动化 |

## 9. 测试指标

### 9.1 过程指标
- 测试用例执行率
- 缺陷发现率
- 测试覆盖率
- 自动化测试比例

### 9.2 质量指标
- 缺陷密度
- 缺陷修复时间
- 回归测试通过率
- 生产环境缺陷率

### 9.3 指标看板
```kotlin
// 测试报告生成
class TestMetricsReporter {
    fun generateDashboard(): TestDashboard {
        return TestDashboard(
            coverage = CoverageMetrics(
                line = 82.5f,
                branch = 75.3f,
                method = 88.2f
            ),
            testResults = TestResults(
                total = 1250,
                passed = 1235,
                failed = 10,
                skipped = 5
            ),
            performance = PerformanceMetrics(
                avgStartupTime = 2.3f,
                avgMemoryUsage = 125f,
                avgResponseTime = 1.8f
            )
        )
    }
}
```

## 10. 风险管理

### 10.1 测试风险识别
| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| AI服务不稳定 | 高 | 中 | Mock数据、降级策略 |
| 设备碎片化 | 中 | 高 | 扩大测试设备范围 |
| 测试数据不足 | 中 | 中 | 建立测试数据工厂 |
| 自动化维护成本高 | 中 | 中 | 使用Page Object模式 |

### 10.2 应急预案
1. **关键缺陷处理流程**
   - P0缺陷：立即修复，热修复发布
   - P1缺陷：24小时内修复
   - P2缺陷：下个版本修复

2. **测试阻塞处理**
   - 环境问题：备用环境切换
   - 数据问题：使用Mock数据
   - 工具问题：手动测试补充

---

*本文档将根据项目进展持续更新*