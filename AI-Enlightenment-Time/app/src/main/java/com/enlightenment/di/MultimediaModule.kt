package com.enlightenment.di

import com.enlightenment.multimedia.audio.AudioService
import com.enlightenment.multimedia.audio.AudioServiceImpl
import com.enlightenment.multimedia.camera.CameraService
import com.enlightenment.multimedia.camera.CameraServiceImpl

/**
 * 多媒体服务依赖注入模块
 */


abstract class MultimediaModule {
    
    @Binds
    
    abstract fun bindCameraService(
        cameraServiceImpl: CameraServiceImpl
    ): CameraService
    
    @Binds
    
    abstract fun bindAudioService(
        audioServiceImpl: AudioServiceImpl
    ): AudioService
}