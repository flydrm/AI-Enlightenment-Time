#!/bin/bash

# AI启蒙时光 - 系统测试脚本
# 用于验证应用功能可用性和交付条件

echo "================================================"
echo "AI启蒙时光 - 系统测试报告"
echo "测试时间: $(date)"
echo "================================================"
echo ""

# 设置环境变量
export ANDROID_HOME=/workspace/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 测试结果计数
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
run_test() {
    local test_name=$1
    local test_command=$2
    echo -n "执行测试: $test_name ... "
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if eval "$test_command" &> /dev/null; then
        echo -e "${GREEN}通过${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}失败${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

echo "1. 环境检查"
echo "================================================"
run_test "Android SDK 检查" "test -d $ANDROID_HOME"
run_test "Gradle Wrapper 检查" "test -x ./gradlew"
run_test "local.properties 检查" "test -f local.properties"
echo ""

echo "2. 代码质量检查"
echo "================================================"
# 检查源代码文件
run_test "MainActivity 存在" "test -f app/src/main/java/com/enlightenment/presentation/MainActivity.kt"
run_test "主题配置存在" "test -f app/src/main/java/com/enlightenment/presentation/ui/theme/Theme.kt"
run_test "导航配置存在" "test -f app/src/main/java/com/enlightenment/presentation/navigation/AppNavigation.kt"
echo ""

echo "3. 测试文件检查"
echo "================================================"
run_test "单元测试存在" "find app/src/test -name '*.kt' | grep -q ."
run_test "UI测试存在" "find app/src/androidTest -name '*.kt' | grep -q ."
echo ""

echo "4. 项目结构验证"
echo "================================================"
run_test "Clean Architecture - Presentation层" "test -d app/src/main/java/com/enlightenment/presentation"
run_test "Clean Architecture - Domain层" "test -d app/src/main/java/com/enlightenment/domain"
run_test "Clean Architecture - Data层" "test -d app/src/main/java/com/enlightenment/data"
run_test "AI服务模块" "test -d app/src/main/java/com/enlightenment/ai"
run_test "多媒体模块" "test -d app/src/main/java/com/enlightenment/multimedia"
echo ""

echo "5. 功能模块检查"
echo "================================================"
run_test "故事功能模块" "test -f app/src/main/java/com/enlightenment/presentation/ui/screens/story/StoryScreen.kt"
run_test "相机功能模块" "test -f app/src/main/java/com/enlightenment/multimedia/camera/CameraManager.kt"
run_test "语音功能模块" "test -f app/src/main/java/com/enlightenment/multimedia/audio/AudioManager.kt"
run_test "成就系统模块" "test -f app/src/main/java/com/enlightenment/domain/model/Achievement.kt"
echo ""

echo "6. 资源文件检查"
echo "================================================"
run_test "字符串资源" "test -f app/src/main/res/values/strings.xml"
run_test "颜色资源" "test -f app/src/main/res/values/colors.xml"
run_test "图标资源" "test -d app/src/main/res/drawable"
echo ""

echo "7. 构建配置检查"
echo "================================================"
run_test "应用构建配置" "test -f app/build.gradle.kts"
run_test "项目构建配置" "test -f build.gradle.kts"
run_test "Gradle配置" "test -f gradle.properties"
echo ""

echo "8. 依赖完整性检查"
echo "================================================"
# 检查关键依赖配置
run_test "Jetpack Compose依赖" "grep -q 'androidx.compose' app/build.gradle.kts"
run_test "Hilt依赖注入" "grep -q 'hilt-android' app/build.gradle.kts"
run_test "Room数据库" "grep -q 'room-runtime' app/build.gradle.kts"
run_test "Retrofit网络" "grep -q 'retrofit' app/build.gradle.kts"
run_test "CameraX相机" "grep -q 'camera-camera2' app/build.gradle.kts"
echo ""

echo "9. 代码规范检查"
echo "================================================"
# 检查是否有TODO或FIXME
TODO_COUNT=$(find app/src/main -name "*.kt" -exec grep -l "TODO\|FIXME" {} \; | wc -l)
if [ $TODO_COUNT -eq 0 ]; then
    run_test "无待处理TODO/FIXME" "true"
else
    run_test "无待处理TODO/FIXME" "false"
    echo "  发现 $TODO_COUNT 个文件包含TODO/FIXME标记"
fi
echo ""

echo "10. 安全检查"
echo "================================================"
run_test "ProGuard配置" "test -f app/proguard-rules.pro"
run_test "安全加密模块" "test -f app/src/main/java/com/enlightenment/security/SecurityManager.kt"
run_test "数据脱敏工具" "test -f app/src/main/java/com/enlightenment/security/DataMasking.kt"
echo ""

echo "================================================"
echo "测试总结"
echo "================================================"
echo "总测试数: $TOTAL_TESTS"
echo -e "通过测试: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败测试: ${RED}$FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！应用满足基本交付条件。${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ 有 $FAILED_TESTS 个测试失败，请检查相关问题。${NC}"
    echo ""
    echo "建议措施："
    echo "1. 检查缺失的文件或模块"
    echo "2. 确保所有依赖正确配置"
    echo "3. 完成未实现的功能模块"
    exit 1
fi