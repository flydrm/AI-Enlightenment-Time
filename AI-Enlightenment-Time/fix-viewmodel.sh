#!/bin/bash

# 修复ViewModel引用
echo "开始修复ViewModel引用..."

# 替换hiltViewModel引用
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel()/remember { HomeViewModel(DIContainer.storyRepository, DIContainer.userProgressRepository) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<HomeViewModel>()/remember { HomeViewModel(DIContainer.storyRepository, DIContainer.userProgressRepository) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<StoryViewModel>()/remember { StoryViewModel(DIContainer.storyRepository, DIContainer.generateStoryUseCase) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<CameraViewModel>()/remember { CameraViewModel(android.app.Application()) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<VoiceViewModel>()/remember { VoiceViewModel(android.app.Application()) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<SettingsViewModel>()/remember { SettingsViewModel() }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<AchievementViewModel>()/remember { AchievementViewModel(DIContainer.userProgressRepository) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<ParentAuthViewModel>()/remember { ParentAuthViewModel(DIContainer.userPreferences) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<ParentDashboardViewModel>()/remember { ParentDashboardViewModel(DIContainer.userProgressRepository) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<LearningReportViewModel>()/remember { LearningReportViewModel(DIContainer.userProgressRepository) }/g' {} +
find app/src/main/java -name "*.kt" -type f -exec sed -i 's/hiltViewModel<StoryPlayerViewModel>()/remember { StoryPlayerViewModel(android.app.Application(), DIContainer.storyRepository) }/g' {} +

# 移除hilt相关导入
find app/src/main/java -name "*.kt" -type f -exec sed -i '/import.*hiltViewModel/d' {} +

# 添加remember导入
find app/src/main/java -name "*.kt" -type f -exec sed -i '/import androidx.compose.runtime.*/a\
import androidx.compose.runtime.remember' {} +

echo "ViewModel引用修复完成！"