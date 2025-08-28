#!/bin/bash

# 清理重复的导入
echo "开始清理导入..."

# 移除重复的导入行
find app/src/main/java -name "*.kt" -type f -exec awk '!seen[$0]++' {} > {}.tmp && mv {}.tmp {} \;

# 移除连续的空行
find app/src/main/java -name "*.kt" -type f -exec sed -i '/^$/N;/^\n$/d' {} +

echo "导入清理完成！"