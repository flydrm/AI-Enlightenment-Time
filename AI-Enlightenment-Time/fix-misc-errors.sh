#!/bin/bash

# 修复其他杂项错误
echo "开始修复其他错误..."

# 修复BorderStroke导入
find app/src/main/java -name "*.kt" -type f -exec sed -i '1s/^/import androidx.compose.foundation.BorderStroke\n/' {} +

# 修复Stroke导入
find app/src/main/java -name "*.kt" -type f -exec sed -i '1s/^/import androidx.compose.ui.graphics.drawscope.Stroke\n/' {} +

# 修复实验性API注解
find app/src/main/java -name "*.kt" -type f -exec sed -i '/This is an experimental animation API/i\
@OptIn(ExperimentalAnimationApi::class)' {} +

find app/src/main/java -name "*.kt" -type f -exec sed -i '/This material API is experimental/i\
@OptIn(ExperimentalMaterial3Api::class)' {} +

# 添加必要的导入
find app/src/main/java -name "*.kt" -type f -exec sed -i '1s/^/import androidx.compose.animation.ExperimentalAnimationApi\n/' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i '1s/^/import androidx.compose.material3.ExperimentalMaterial3Api\n/' {} +

# 修复Chapter引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Chapter/Story/g' {} +

# 修复Loading引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/Loading/"Loading"/g' {} +

# 修复ic_notification_panda和ic_play引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/ic_notification_panda/ic_launcher/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/ic_play/ic_launcher/g' {} +

# 修复minutesSpent引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/\.minutesSpent/.totalMinutesSpent/g' {} +

# 修复isDailyReminderEnabled
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/isDailyReminderEnabled/true/g' {} +

echo "其他错误修复完成！"