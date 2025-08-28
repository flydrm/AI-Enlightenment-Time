package com.enlightenment.di

import com.enlightenment.data.repository.StoryRepositoryImpl
import com.enlightenment.data.repository.UserProgressRepositoryImpl
import com.enlightenment.domain.repository.StoryRepository
import com.enlightenment.domain.repository.UserProgressRepository



abstract class RepositoryModule {
    
    @Binds
    
    abstract fun bindStoryRepository(
        storyRepositoryImpl: StoryRepositoryImpl
    ): StoryRepository
    
    @Binds
    
    abstract fun bindUserProgressRepository(
        userProgressRepositoryImpl: UserProgressRepositoryImpl
    ): UserProgressRepository
}
