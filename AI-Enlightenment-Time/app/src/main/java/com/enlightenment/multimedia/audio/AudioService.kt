package com.enlightenment.multimedia.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow



/**
 * 音频服务接口
 */
interface AudioService {
    /**
     * 音频录制状态
     */
    val recordingState: StateFlow<RecordingState>
    
    /**
     * 开始录音
     */
    suspend fun startRecording(): Flow<AudioData>
    
    /**
     * 停止录音
     */
    fun stopRecording()
    
    /**
     * 播放音频
     */
    suspend fun playAudio(audioData: ByteArray)
    
    /**
     * 停止播放
     */
    fun stopPlayback()
    
    /**
     * 获取音频振幅（用于显示音波动画）
     */
    fun getAudioAmplitude(): Flow<Float>
    
    /**
     * 初始化服务
     */
    suspend fun initialize(context: Context)
    
    /**
     * 释放资源
     */
    fun release()
}
/**
 * 录音状态
 */
sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Processing : RecordingState()
    data class Error(val message: String) : RecordingState()
}
/**
 * 音频数据
 */
data class AudioData(
    val data: ByteArray,
    val sampleRate: Int,
    val channelConfig: Int,
    val audioFormat: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as AudioData
        
        if (!data.contentEquals(other.data)) return false
        if (sampleRate != other.sampleRate) return false
        if (channelConfig != other.channelConfig) return false
        if (audioFormat != other.audioFormat) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channelConfig
        result = 31 * result + audioFormat
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
