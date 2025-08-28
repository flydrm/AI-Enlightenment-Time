package com.enlightenment.di

import com.enlightenment.data.repository.StoryRepositoryImpl
import com.enlightenment.data.repository.UserProgressRepositoryImpl
import com.enlightenment.domain.repository.StoryRepository
import com.enlightenment.domain.repository.UserProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindStoryRepository(
        storyRepositoryImpl: StoryRepositoryImpl
    ): StoryRepository
    
    @Binds
    @Singleton
    abstract fun bindUserProgressRepository(
        userProgressRepositoryImpl: UserProgressRepositoryImpl
    ): UserProgressRepository
}