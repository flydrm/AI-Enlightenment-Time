package com.enlightenment.di

import com.enlightenment.multimedia.audio.AudioService
import com.enlightenment.multimedia.audio.AudioServiceImpl
import com.enlightenment.multimedia.camera.CameraService
import com.enlightenment.multimedia.camera.CameraServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 多媒体服务依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MultimediaModule {
    
    @Binds
    @Singleton
    abstract fun bindCameraService(
        cameraServiceImpl: CameraServiceImpl
    ): CameraService
    
    @Binds
    @Singleton
    abstract fun bindAudioService(
        audioServiceImpl: AudioServiceImpl
    ): AudioService
}