#!/bin/bash

echo "执行最终修复..."

# 1. 删除重复的文件
echo "删除重复文件..."
rm -f app/src/main/java/com/enlightenment/presentation/MainActivity.kt
rm -f app/src/main/java/com/enlightenment/presentation/SimpleMainActivity.kt
rm -f app/src/main/java/com/enlightenment/presentation/SimpleMainActivityNoHilt.kt

# 2. 删除重复的数据类定义
echo "删除重复定义..."
# 删除重复的PlayerState
rm -f app/src/main/java/com/enlightenment/presentation/story/player/PlayerState.kt
rm -f app/src/main/java/com/enlightenment/presentation/voice/VoiceState.kt
rm -f app/src/main/java/com/enlightenment/presentation/voice/ConversationMessage.kt
rm -f app/src/main/java/com/enlightenment/presentation/parent/AuthMethod.kt
rm -f app/src/main/java/com/enlightenment/presentation/ui/screens/home/HomeFeature.kt

# 3. 修复文件格式问题
echo "修复文件格式..."
find app/src/main/java -name "*.kt" -type f | while read file; do
    # 修复 @file:OptIn 的位置
    if grep -q "@file:OptIn" "$file"; then
        # 提取package
        pkg=$(grep "^package " "$file" | head -1)
        # 提取@file注解
        opt_in=$(grep "@file:OptIn" "$file" | head -1)
        # 提取imports
        imports=$(grep "^import " "$file" | grep -v "@file:OptIn")
        # 提取内容
        content=$(grep -v "^package \|^@file:OptIn\|^import " "$file")
        
        # 重建文件
        echo "$pkg" > "$file.tmp"
        echo "" >> "$file.tmp"
        echo "@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)" >> "$file.tmp"
        echo "" >> "$file.tmp"
        echo "$imports" >> "$file.tmp"
        echo "" >> "$file.tmp"
        echo "$content" >> "$file.tmp"
        
        mv "$file.tmp" "$file"
    fi
done

# 4. 修复具体的语法错误
echo "修复语法错误..."

# 修复 "is PlayerState.PLAYING" 为 "== PlayerState.PLAYING"
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/is PlayerState\.PLAYING/== PlayerState.PLAYING/g
s/is PlayerState\.PAUSED/== PlayerState.PAUSED/g
s/is PlayerState\.LOADING/== PlayerState.LOADING/g
s/is PlayerState\.Playing/== PlayerState.PLAYING/g
s/is PlayerState\.Paused/== PlayerState.PAUSED/g
s/is VoiceState\.LISTENING/== VoiceState.LISTENING/g
s/is VoiceState\.PROCESSING/== VoiceState.PROCESSING/g
s/is VoiceState\.SPEAKING/== VoiceState.SPEAKING/g
s/is VoiceState\.Listening/== VoiceState.LISTENING/g
s/is VoiceState\.Processing/== VoiceState.PROCESSING/g
s/is VoiceState\.Speaking/== VoiceState.SPEAKING/g
' {} +

# 修复 Use of enum entry names as types
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/Use of enum entry names as types is not allowed, use enum type instead//g
' {} +

# 修复 'is' over enum entry is not allowed
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/'\''is'\'' over enum entry is not allowed, use comparison instead//g
' {} +

# 5. 修复缺失的属性和方法
echo "修复缺失的属性..."

# 修复ParentAuthScreen的TextFieldDefaults问题
sed -i 's/\[\[/TextFieldDefaults.outlinedTextFieldColors(/g' app/src/main/java/com/enlightenment/presentation/parent/ParentAuthScreen.kt
sed -i 's/\]\]/)/g' app/src/main/java/com/enlightenment/presentation/parent/ParentAuthScreen.kt

# 修复AssistChip的双重引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/AssistAssistChip/AssistChip/g' {} +

# 修复FilterChip引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/FilterAssistChip/FilterChip/g' {} +

# 6. 创建缺失的类
echo "创建缺失的类..."

# 创建ChildFriendlyTypography
cat >> app/src/main/java/com/enlightenment/presentation/ui/theme/Type.kt << 'EOF'

val ChildFriendlyTypography = Typography
EOF

# 创建DailyLearningData
cat > app/src/main/java/com/enlightenment/presentation/parent/DailyLearningData.kt << 'EOF'
package com.enlightenment.presentation.parent

data class DailyLearningData(
    val day: String,
    val minutes: Int
)
EOF

# 创建SkillProgress
cat > app/src/main/java/com/enlightenment/presentation/parent/SkillProgress.kt << 'EOF'
package com.enlightenment.presentation.parent

data class SkillProgress(
    val skillName: String,
    val progress: Int
)
EOF

# 7. 修复ViewModel引用
echo "修复ViewModel引用..."
find app/src/main/java -name "*Screen.kt" -type f -exec sed -i '
s/val viewModel.*=.*remember { HomeViewModel() }/val viewModel = remember { HomeViewModel() }/g
s/Type mismatch: inferred type is Unit but \([A-Za-z]*ViewModel\) was expected/val viewModel = remember { \1() }/g
s/Unresolved reference: HomeViewModel//g
' {} +

# 8. 添加必要的导入
echo "添加导入..."
find app/src/main/java -name "*Screen.kt" -type f | while read file; do
    if ! grep -q "import androidx.compose.material3.FilterChip" "$file" && grep -q "FilterChip" "$file"; then
        sed -i '/^import androidx.compose.material3/a\
import androidx.compose.material3.FilterChip' "$file"
    fi
done

echo "最终修复完成！"