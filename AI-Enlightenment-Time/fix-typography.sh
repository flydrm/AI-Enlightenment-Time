#!/bin/bash

# 修复Typography引用错误
echo "开始修复Typography引用..."

# 替换所有错误的Typography引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Typography\.headlineMedium/MaterialTheme.typography.headlineMedium/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Typography\.titleLarge/MaterialTheme.typography.titleLarge/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Typography\.titleMedium/MaterialTheme.typography.titleMedium/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Typography\.bodyMedium/MaterialTheme.typography.bodyMedium/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Typography\.bodySmall/MaterialTheme.typography.bodySmall/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Typography\.bodyLarge/MaterialTheme.typography.bodyLarge/g' {} +

# 修复独立的typography引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.headlineMedium/MaterialTheme.typography.headlineMedium/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.titleLarge/MaterialTheme.typography.titleLarge/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.titleMedium/MaterialTheme.typography.titleMedium/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.bodyMedium/MaterialTheme.typography.bodyMedium/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.bodySmall/MaterialTheme.typography.bodySmall/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.bodyLarge/MaterialTheme.typography.bodyLarge/g' {} +

# 移除错误的Typography导入
find app/src/main/java -name "*.kt" -type f -exec sed -i '/import.*Typography$/d' {} +

echo "Typography引用修复完成！"