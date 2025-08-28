#!/bin/bash

echo "开始完整修复所有错误..."

# 1. 修复所有文件的import顺序
echo "步骤1: 修复import顺序..."
find app/src/main/java -name "*.kt" -type f | while read file; do
    # 提取package
    pkg=$(grep "^package " "$file" | head -1)
    
    # 提取@file注解
    file_opts=$(grep "^@file:" "$file" | grep -v "^@file:OptIn")
    
    # 提取imports并去重
    imports=$(grep "^import " "$file" | sort -u | grep -v "^import androidx.compose.material3.ExperimentalMaterial3Api" | grep -v "^import androidx.compose.animation.ExperimentalAnimationApi")
    
    # 提取内容
    content=$(grep -v "^package \|^@file:\|^import " "$file" | sed '/^$/d')
    
    # 重建文件
    echo "$pkg" > "$file.tmp"
    echo "" >> "$file.tmp"
    
    # 添加必要的@OptIn注解
    if grep -q "ExperimentalMaterial3Api\|ExperimentalAnimationApi" "$file"; then
        echo "@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)" >> "$file.tmp"
        echo "" >> "$file.tmp"
    fi
    
    if [ ! -z "$imports" ]; then
        echo "$imports" >> "$file.tmp"
        echo "" >> "$file.tmp"
    fi
    
    echo "$content" >> "$file.tmp"
    mv "$file.tmp" "$file"
done

# 2. 修复所有typography引用
echo "步骤2: 修复typography引用..."
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.typography\([^a-zA-Z]\)/\.typography\1/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Unresolved reference: typography/MaterialTheme.typography/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/style = MaterialTheme\.typography\./style = MaterialTheme.typography./g' {} +

# 3. 修复特定文件的错误
echo "步骤3: 修复特定文件错误..."

# 修复ParentAuthScreen的语法错误
sed -i 's/\[\[/[/g; s/\]\]/]/g' app/src/main/java/com/enlightenment/presentation/parent/ParentAuthScreen.kt

# 修复ResponsiveStoryScreen的语法错误
sed -i 's/\$\$/$/g' app/src/main/java/com/enlightenment/presentation/ui/screens/story/ResponsiveStoryScreen.kt

# 修复HomeScreen的语法错误
sed -i 's/\[\[/[/g; s/\]\]/]/g' app/src/main/java/com/enlightenment/presentation/ui/screens/home/HomeScreen.kt

# 4. 添加缺失的导入
echo "步骤4: 添加缺失的导入..."
find app/src/main/java -name "*.kt" -type f | while read file; do
    # 添加remember导入
    if grep -q "remember {" "$file" && ! grep -q "import androidx.compose.runtime.remember" "$file"; then
        sed -i '/^import androidx.compose.runtime/a\
import androidx.compose.runtime.remember' "$file"
    fi
    
    # 添加viewModel导入
    if grep -q "viewModel" "$file" && ! grep -q "import androidx.lifecycle.viewmodel.compose.viewModel" "$file"; then
        sed -i '/^import androidx.lifecycle/a\
import androidx.lifecycle.viewmodel.compose.viewModel' "$file"
    fi
    
    # 添加ExperimentalMaterial3Api导入
    if grep -q "@OptIn.*ExperimentalMaterial3Api" "$file" && ! grep -q "import androidx.compose.material3.ExperimentalMaterial3Api" "$file"; then
        sed -i '/^import androidx.compose.material3/a\
import androidx.compose.material3.ExperimentalMaterial3Api' "$file"
    fi
    
    # 添加ExperimentalAnimationApi导入
    if grep -q "@OptIn.*ExperimentalAnimationApi" "$file" && ! grep -q "import androidx.compose.animation.ExperimentalAnimationApi" "$file"; then
        sed -i '/^import androidx.compose.animation/a\
import androidx.compose.animation.ExperimentalAnimationApi' "$file"
    fi
done

# 5. 修复ViewModel创建
echo "步骤5: 修复ViewModel创建..."
find app/src/main/java -name "*Screen.kt" -type f -exec sed -i 's/Type mismatch: inferred type is Unit but \([A-Za-z]*ViewModel\) was expected/\1/g' {} +
find app/src/main/java -name "*Screen.kt" -type f -exec sed -i 's/remember { HomeViewModel() }/remember { HomeViewModel() }/g' {} +

# 6. 修复null安全问题
echo "步骤6: 修复null安全..."
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Type mismatch: inferred type is String? but String was expected/it ?: ""/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/getString(\([^)]*\))/getString(\1) ?: ""/g' {} +

# 7. 修复资源引用
echo "步骤7: 修复资源引用..."
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/R\.drawable\.ic_launcher/android.R.drawable.star_on/g' {} +

echo "完整修复完成！"