#!/bin/bash

# 移除Hilt注解的脚本
echo "开始移除Hilt注解..."

# 备份原文件
mkdir -p backup-before-remove-hilt
cp -r app/src/main/java backup-before-remove-hilt/

# 移除所有Hilt相关的import语句
find app/src/main/java -name "*.kt" -type f -exec sed -i '/^import dagger\./d' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i '/^import javax\.inject\./d' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i '/import.*hilt/d' {} +

# 移除Hilt注解
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@HiltAndroidApp//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@AndroidEntryPoint//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@HiltViewModel//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@Inject constructor/constructor/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@Inject //g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@Module//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@InstallIn([^)]*)//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@Provides//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@Singleton//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@ApplicationContext //g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@AssistedInject//g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@Assisted //g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/@HiltWorker//g' {} +

echo "Hilt注解移除完成！"