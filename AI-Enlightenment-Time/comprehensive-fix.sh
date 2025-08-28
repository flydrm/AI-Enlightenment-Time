#!/bin/bash

echo "开始综合修复..."

# 1. 修复所有文件的package和import顺序
echo "修复文件结构..."
find app/src/main/java -name "*.kt" -type f | while read file; do
    # 提取package语句
    pkg=$(grep "^package " "$file" | head -1)
    
    # 提取并排序imports
    imports=$(grep "^import " "$file" | sort -u | grep -v "androidx.compose.material3.ExperimentalMaterial3Api" | grep -v "androidx.compose.animation.ExperimentalAnimationApi" | grep -v "androidx.compose.foundation.BorderStroke" | grep -v "androidx.compose.ui.graphics.drawscope.Stroke")
    
    # 提取其余内容
    content=$(grep -v "^package " "$file" | grep -v "^import " | sed '/^$/d')
    
    # 重建文件
    echo "$pkg" > "$file"
    echo "" >> "$file"
    if [ ! -z "$imports" ]; then
        echo "$imports" >> "$file"
        echo "" >> "$file"
    fi
    echo "$content" >> "$file"
done

# 2. 修复具体的编译错误
echo "修复编译错误..."

# 修复context引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/context.applicationContext as Application/context as Application/g' {} +

# 修复MainActivity引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/MainActivity::class.java/MainActivityNoHilt::class.java/g' {} +

# 修复资源引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/R.drawable.ic_notification_panda/android.R.drawable.star_on/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/R.drawable.ic_play/android.R.drawable.ic_media_play/g' {} +

# 移除错误的remember函数调用中的泛型
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/remember<[^>]*> {/remember {/g' {} +

# 修复ViewModel创建
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/remember { HomeViewModel(DIContainer.storyRepository, DIContainer.userProgressRepository) }/viewModel { HomeViewModel() }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/remember { \([^}]*\) }/viewModel { \1 }/g' {} +

# 添加viewModel导入
find app/src/main/java -name "*Screen.kt" -type f -exec sed -i '/^import/a\
import androidx.lifecycle.viewmodel.compose.viewModel' {} +

# 移除错误的Stroke导入
find app/src/main/java -name "*.kt" -type f -exec sed -i '/import androidx.compose.ui.graphics.drawscope.Stroke/d' {} +

echo "综合修复完成！"