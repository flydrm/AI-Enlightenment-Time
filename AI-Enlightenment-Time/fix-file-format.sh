#!/bin/bash

# 修复文件格式问题
echo "开始修复文件格式..."

# 移除文件开头的错误格式
find app/src/main/java -name "*.kt" -type f -exec sed -i '1s/^import.*$//' {} +

# 移除重复的import语句（保留原始的）
find app/src/main/java -name "*.kt" -type f | while read file; do
    # 获取所有import语句
    imports=$(grep "^import " "$file" | sort -u)
    # 获取非import内容
    content=$(grep -v "^import " "$file")
    
    # 重写文件
    echo "package $(grep "^package " "$file" | sed 's/package //')" > "$file.tmp"
    echo "" >> "$file.tmp"
    echo "$imports" >> "$file.tmp"
    echo "" >> "$file.tmp"
    echo "$content" | grep -v "^package " >> "$file.tmp"
    
    mv "$file.tmp" "$file"
done

echo "文件格式修复完成！"