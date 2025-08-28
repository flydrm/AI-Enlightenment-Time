#!/bin/bash

echo "执行最终错误修复..."

# 1. 修复所有Screen文件中的viewModel引用
echo "修复viewModel引用..."
find app/src/main/java -name "*Screen.kt" -type f -exec sed -i '
s/val viewModel: \([A-Za-z]*ViewModel\) = remember { \([A-Za-z]*ViewModel\)() }/val viewModel = remember { \1() }/g
s/Type mismatch: inferred type is Unit but \([A-Za-z]*ViewModel\) was expected/val viewModel = remember { \1() }/g
s/Unresolved reference: HomeViewModel/}/g
' {} +

# 2. 修复所有的MaterialTheme.typography引用
echo "修复所有typography引用..."
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/Unresolved reference: typography/MaterialTheme.typography/g
s/\.typography\./\.typography\./g
s/MaterialTheme\.typography\.typography/MaterialTheme.typography/g
' {} +

# 3. 修复Chip组件的使用
echo "修复Chip组件..."
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/Cannot access '\''Chip'\'': it is private in file/AssistChip/g
s/Chip(/AssistChip(/g
' {} +

# 4. 修复缺失的属性
echo "添加缺失的属性..."
sed -i '/data class Story(/,/^)/ {
    /^)/ i\    val chapters: List<Chapter> = emptyList()
}' app/src/main/java/com/enlightenment/domain/model/Story.kt

# 5. 创建Chapter数据类
cat > app/src/main/java/com/enlightenment/domain/model/Chapter.kt << 'EOF'
package com.enlightenment.domain.model

data class Chapter(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String? = null
)
EOF

# 6. 修复ParentAuthScreen的语法错误
echo "修复ParentAuthScreen..."
sed -i '
s/\[\[/TextFieldDefaults.outlinedTextFieldColors(/g
s/\]\]/)/g
s/Expecting an element//g
s/Expecting '\'')'\''/)/g
' app/src/main/java/com/enlightenment/presentation/parent/ParentAuthScreen.kt

# 7. 修复ResponsiveStoryScreen的语法错误
echo "修复ResponsiveStoryScreen..."
sed -i '
s/Expecting property name or receiver type//g
s/Type expected//g
s/Expecting an expression//g
s/\$\$/$/g
' app/src/main/java/com/enlightenment/presentation/ui/screens/story/ResponsiveStoryScreen.kt

# 8. 修复AnimationShowcase的key参数
echo "修复AnimationShowcase..."
sed -i 's/Cannot find a parameter with this name: key/animationSpec = keyframes {/g' \
    app/src/main/java/com/enlightenment/presentation/ui/screens/AnimationShowcase.kt

# 9. 修复PlayerState引用
echo "修复PlayerState引用..."
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/Unresolved reference: Playing/PlayerState.PLAYING/g
s/Unresolved reference: Paused/PlayerState.PAUSED/g
s/Unresolved reference: Finished/PlayerState.FINISHED/g
s/Unresolved reference: Loading/PlayerState.LOADING/g
s/Unresolved reference: Error/PlayerState.ERROR/g
' {} +

# 10. 修复VoiceState引用
echo "修复VoiceState引用..."
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/Unresolved reference: Listening/VoiceState.LISTENING/g
s/Unresolved reference: Processing/VoiceState.PROCESSING/g
s/Unresolved reference: Speaking/VoiceState.SPEAKING/g
' {} +

# 11. 修复EnhancedAnimatedPanda的参数
echo "修复EnhancedAnimatedPanda参数..."
find app/src/main/java -name "*.kt" -type f -exec sed -i '
s/Cannot find a parameter with this name: speech/mood = /g
s/Cannot find a parameter with this name: isActive/mood = /g
' {} +

# 12. 修复缺失的导入
echo "添加缺失的导入..."
find app/src/main/java -name "*.kt" -type f | while read file; do
    # 检查是否使用了BorderStroke
    if grep -q "BorderStroke" "$file" && ! grep -q "import androidx.compose.foundation.BorderStroke" "$file"; then
        sed -i '/^import androidx.compose/a\
import androidx.compose.foundation.BorderStroke' "$file"
    fi
    
    # 检查是否使用了TextFieldDefaults
    if grep -q "TextFieldDefaults" "$file" && ! grep -q "import androidx.compose.material3.TextFieldDefaults" "$file"; then
        sed -i '/^import androidx.compose.material3/a\
import androidx.compose.material3.TextFieldDefaults' "$file"
    fi
    
    # 检查是否使用了AssistChip
    if grep -q "AssistChip" "$file" && ! grep -q "import androidx.compose.material3.AssistChip" "$file"; then
        sed -i '/^import androidx.compose.material3/a\
import androidx.compose.material3.AssistChip' "$file"
    fi
done

echo "最终错误修复完成！"