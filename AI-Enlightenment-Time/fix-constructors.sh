#!/bin/bash

# 修复构造函数
echo "开始修复构造函数..."

# 修复ViewModel构造函数
find app/src/main/java -name "*ViewModel.kt" -type f -exec sed -i 's/class \([A-Za-z]*ViewModel\) constructor(/class \1(/g' {} +

# 修复其他类的构造函数
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/class \([A-Za-z]*\) constructor(/class \1(/g' {} +

echo "构造函数修复完成！"