# AI启蒙时光 - UI设计指南

## 文档信息
- **版本**: 1.0
- **日期**: 2024-12-30
- **设计风格**: 温暖童趣风格
- **目标用户**: 3-6岁儿童

## 目录
1. [设计理念](#1-设计理念)
2. [视觉识别系统](#2-视觉识别系统)
3. [色彩系统](#3-色彩系统)
4. [字体系统](#4-字体系统)
5. [图标设计](#5-图标设计)
6. [组件库](#6-组件库)
7. [布局系统](#7-布局系统)
8. [动画规范](#8-动画规范)
9. [响应式设计](#9-响应式设计)
10. [无障碍设计](#10-无障碍设计)

## 1. 设计理念

### 1.1 核心价值
- **温暖陪伴**: 营造安全、友好的学习环境
- **趣味互动**: 通过游戏化设计激发学习兴趣
- **简单易用**: 符合儿童认知和操作习惯

### 1.2 设计原则
1. **大而清晰**: 所有元素都要足够大，易于识别
2. **色彩鲜明**: 使用高饱和度的暖色调
3. **圆润友好**: 避免尖锐边角，使用圆角设计
4. **即时反馈**: 每个操作都有视觉和声音反馈
5. **防误操作**: 重要功能需要二次确认

## 2. 视觉识别系统

### 2.1 品牌标识
- **应用名称**: AI启蒙时光
- **吉祥物**: 红色小熊猫"小红"
- **标语**: "每天15分钟，快乐学习每一天"

### 2.2 小熊猫形象设计
```
特征描述：
- 主色调：活力红（#E53935）
- 体型：圆润可爱，头身比例1:1
- 表情：大眼睛，微笑嘴型
- 配饰：可选（帽子、围巾等）

表情系统：
- 开心 😊：默认状态
- 惊喜 😮：发现新内容
- 思考 🤔：等待回答
- 鼓励 👍：完成任务
- 困倦 😴：休息时间
```

## 3. 色彩系统

### 3.1 主色板
```kotlin
// 主色调
val PrimaryRed = Color(0xFFE53935)      // 活力红 - 主品牌色
val SoftRed = Color(0xFFEF5350)         // 柔和红 - 次要元素
val LightRed = Color(0xFFFFCDD2)        // 浅红 - 背景色

// 辅助色
val SkyBlue = Color(0xFF87CEEB)        // 天空蓝 - 对比色
val GrassGreen = Color(0xFF8BC34A)     // 草地绿 - 成功状态
val SunYellow = Color(0xFFFFEB3B)      // 阳光黄 - 高亮/奖励
val WoodBrown = Color(0xFF795548)      // 木纹棕 - 文字/边框

// 背景色
val CreamWhite = Color(0xFFFFF8E1)     // 奶油白 - 主背景
val CloudGray = Color(0xFFF5F5F5)      // 云朵灰 - 次背景
```

### 3.2 色彩应用规则
- **红色系**: 用于主要按钮、标题、重要提示
- **蓝色系**: 用于链接、次要操作
- **绿色系**: 用于成功反馈、完成状态
- **黄色系**: 用于奖励、成就、星星评分

## 4. 字体系统

### 4.1 字体选择
```kotlin
// 字体定义
val Typography = Typography(
    // 大标题 - 用于主页标题
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.5.sp
    ),
    
    // 中标题 - 用于卡片标题
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    
    // 正文 - 故事内容
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    
    // 按钮文字
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        letterSpacing = 1.sp
    )
)
```

### 4.2 字体使用规范
- **最小字号**: 16sp（确保可读性）
- **行高**: 1.5倍字号
- **字重**: 避免过细，最小Regular
- **颜色对比**: WCAG AA标准（4.5:1）

## 5. 图标设计

### 5.1 图标风格
- **风格**: 线性图标，圆角处理
- **线宽**: 3dp
- **尺寸**: 24dp（小）、32dp（中）、48dp（大）
- **颜色**: 单色或双色渐变

### 5.2 常用图标库
```
功能图标：
🏠 主页     📚 故事     📷 拍照     🎤 语音
🏆 成就     ⚙️ 设置     👨‍👩‍👦 家长     ❓ 帮助

状态图标：
✅ 完成     ⏰ 进行中   🔒 锁定     ⭐ 收藏
👍 点赞     ❤️ 喜欢     🎁 礼物     🎯 目标
```

## 6. 组件库

### 6.1 按钮组件
```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Large
) {
    val buttonHeight = when (size) {
        ButtonSize.Small -> 48.dp
        ButtonSize.Medium -> 56.dp
        ButtonSize.Large -> 64.dp  // 儿童友好尺寸
    }
    
    Button(
        onClick = onClick,
        modifier = modifier.height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = PrimaryRed,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(buttonHeight / 2),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            style = Typography.button,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
```

### 6.2 卡片组件
```kotlin
@Composable
fun FeatureCard(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = 6.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(80.dp)) {
                icon()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = Typography.h2,
                color = WoodBrown
            )
        }
    }
}
```

### 6.3 对话气泡
```kotlin
@Composable
fun SpeechBubble(
    text: String,
    isLeft: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isLeft) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            shape = SpeechBubbleShape(isLeft),
            color = if (isLeft) Color.White else LightRed,
            elevation = 4.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp),
                style = Typography.body1
            )
        }
    }
}
```

## 7. 布局系统

### 7.1 网格系统
```kotlin
// 响应式网格
fun getGridColumns(screenWidth: Dp): Int {
    return when {
        screenWidth < 360.dp -> 1  // 手机竖屏
        screenWidth < 600.dp -> 2  // 手机横屏
        screenWidth < 840.dp -> 3  // 小平板
        else -> 4                  // 大平板
    }
}
```

### 7.2 间距规范
```kotlin
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

### 7.3 安全区域
- 顶部：状态栏高度 + 16dp
- 底部：导航栏高度 + 16dp
- 左右：16dp（手机）/ 24dp（平板）

## 8. 动画规范

### 8.1 动画时长
```kotlin
object AnimationDuration {
    const val Fast = 200    // 快速反馈
    const val Normal = 300  // 常规动画
    const val Slow = 500    // 复杂动画
}
```

### 8.2 动画类型

#### 进入动画
```kotlin
@Composable
fun FadeInAnimation(
    content: @Composable () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = AnimationDuration.Normal,
            easing = FastOutSlowInEasing
        )
    )
    
    Box(modifier = Modifier.alpha(animatedAlpha)) {
        content()
    }
}
```

#### 按钮点击效果
```kotlin
@Composable
fun BounceClickEffect(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        content()
    }
}
```

## 9. 响应式设计

### 9.1 断点定义
| 设备类型 | 最小宽度 | 布局策略 |
|---------|---------|---------|
| 小手机 | 320dp | 单列，紧凑 |
| 标准手机 | 360dp | 单列，标准 |
| 大手机 | 400dp | 单列，宽松 |
| 小平板 | 600dp | 双列网格 |
| 标准平板 | 768dp | 三列网格 |
| 大平板 | 900dp+ | 四列网格 |

### 9.2 自适应组件示例
```kotlin
@Composable
fun AdaptiveHomeScreen() {
    BoxWithConstraints {
        val screenWidth = maxWidth
        
        when {
            screenWidth < 600.dp -> PhoneLayout()
            screenWidth < 900.dp -> SmallTabletLayout()
            else -> LargeTabletLayout()
        }
    }
}
```

## 10. 无障碍设计

### 10.1 基本要求
1. **触控目标**: 最小64dp x 64dp
2. **颜色对比**: 符合WCAG AA标准
3. **文字大小**: 支持系统字体缩放
4. **语音朗读**: 所有内容可朗读

### 10.2 实现示例
```kotlin
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .semantics {
                contentDescription = text
                role = Role.Button
            }
            .sizeIn(minWidth = 64.dp, minHeight = 64.dp)
    ) {
        Text(
            text = text,
            style = Typography.button
        )
    }
}
```

### 10.3 儿童特殊考虑
- **简化导航**: 减少层级，直达功能
- **视觉提示**: 动画引导操作
- **声音反馈**: 每个操作都有音效
- **错误宽容**: 允许误操作，易于恢复

---

*本指南将根据实际开发反馈持续更新优化*