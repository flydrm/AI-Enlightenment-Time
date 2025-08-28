package com.enlightenment.multimedia.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 音频管理器
 * 负责音频播放、录音、文字转语音等功能
 */
@Singleton
class AudioManager @Inject constructor(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var textToSpeech: TextToSpeech? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _ttsInitialized = MutableStateFlow(false)
    val ttsInitialized: StateFlow<Boolean> = _ttsInitialized.asStateFlow()
    
    private var currentRecordingPath: String? = null
    
    /**
     * 初始化文字转语音
     */
    suspend fun initializeTTS(locale: Locale = Locale.CHINA) = suspendCancellableCoroutine<Boolean> { continuation ->
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _ttsInitialized.value = false
                    continuation.resume(false)
                } else {
                    _ttsInitialized.value = true
                    continuation.resume(true)
                }
            } else {
                _ttsInitialized.value = false
                continuation.resume(false)
            }
        }
    }
    
    /**
     * 播放音频文件
     */
    suspend fun playAudio(uri: Uri) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            stopPlayback()
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                prepareAsync()
                
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                }
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    continuation.resume(Unit)
                }
                
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    continuation.resumeWithException(IOException("播放音频失败"))
                    true
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * 停止播放
     */
    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
    }
    
    /**
     * 暂停播放
     */
    fun pausePlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                _isPlaying.value = false
            }
        }
    }
    
    /**
     * 恢复播放
     */
    fun resumePlayback() {
        mediaPlayer?.apply {
            if (!isPlaying) {
                start()
                _isPlaying.value = true
            }
        }
    }
    
    /**
     * 开始录音
     */
    suspend fun startRecording(): String = suspendCancellableCoroutine { continuation ->
        try {
            val audioFile = createAudioFile()
            currentRecordingPath = audioFile.absolutePath
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                
                prepare()
                start()
                _isRecording.value = true
            }
            
            continuation.resume(audioFile.absolutePath)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * 停止录音
     */
    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            currentRecordingPath
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 文字转语音
     */
    suspend fun speakText(text: String, utteranceId: String = UUID.randomUUID().toString()) = 
        suspendCancellableCoroutine<Unit> { continuation ->
            if (!_ttsInitialized.value || textToSpeech == null) {
                continuation.resumeWithException(IllegalStateException("TTS未初始化"))
                return@suspendCancellableCoroutine
            }
            
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                
                override fun onDone(utteranceId: String?) {
                    continuation.resume(Unit)
                }
                
                override fun onError(utteranceId: String?) {
                    continuation.resumeWithException(IOException("TTS播放失败"))
                }
            })
            
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    
    /**
     * 停止文字转语音
     */
    fun stopTTS() {
        textToSpeech?.stop()
    }
    
    /**
     * 设置TTS语速
     */
    fun setTTSSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }
    
    /**
     * 设置TTS音调
     */
    fun setTTSPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stopPlayback()
        stopRecording()
        textToSpeech?.apply {
            stop()
            shutdown()
        }
        textToSpeech = null
        _ttsInitialized.value = false
    }
    
    /**
     * 创建音频文件
     */
    private fun createAudioFile(): File {
        val audioDir = File(context.filesDir, "audio").apply {
            if (!exists()) mkdirs()
        }
        
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(audioDir, "AUDIO_$timeStamp.aac")
    }
    
    /**
     * 获取音频保存目录
     */
    fun getAudioDirectory(): File {
        return File(context.filesDir, "audio").apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 删除音频文件
     */
    fun deleteAudio(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }
}