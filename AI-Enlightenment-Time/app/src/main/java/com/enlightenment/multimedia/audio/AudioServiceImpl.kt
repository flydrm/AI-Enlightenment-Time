package com.enlightenment.multimedia.audio

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.media.*
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*



/**
 * 音频服务实现
 */
class AudioServiceImpl() : AudioService {
    
    companion object {
        private const val TAG = "AudioService"
        private const val SAMPLE_RATE = 16000 // 16kHz
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
    }
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null
    
    private val audioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override suspend fun initialize(context: Context) {
        // 检查录音权限
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _recordingState.value = RecordingState.Error("缺少录音权限")
            return
        }
    }
    
    override suspend fun startRecording(): Flow<AudioData> = flow {
        if (_recordingState.value is RecordingState.Recording) {
            throw IllegalStateException("Already recording")
        }
        
        _recordingState.value = RecordingState.Recording
        
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            
            if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                throw IllegalStateException("Failed to get min buffer size")
            }
            
            val bufferSize = minBufferSize * BUFFER_SIZE_FACTOR
            
            audioRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_CONFIG)
                            .setEncoding(AUDIO_FORMAT)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .build()
            } else {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
                )
            }
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("Failed to initialize AudioRecord")
            }
            
            audioRecord?.startRecording()
            
            val buffer = ByteArray(bufferSize)
            
            while (_recordingState.value is RecordingState.Recording && currentCoroutineContext().isActive) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                
                if (readSize > 0) {
                    val audioData = AudioData(
                        data = buffer.copyOf(readSize),
                        sampleRate = SAMPLE_RATE,
                        channelConfig = CHANNEL_CONFIG,
                        audioFormat = AUDIO_FORMAT
                    )
                    emit(audioData)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recording error", e)
            _recordingState.value = RecordingState.Error(e.message ?: "Recording failed")
            throw e
        } finally {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            if (_recordingState.value is RecordingState.Recording) {
                _recordingState.value = RecordingState.Idle
            }
        }
    }.flowOn(Dispatchers.IO)
    
    override fun stopRecording() {
        _recordingState.value = RecordingState.Idle
        recordingJob?.cancel()
        recordingJob = null
    }
    
    override suspend fun playAudio(audioData: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT
            )
            
            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .setEncoding(AUDIO_FORMAT)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AUDIO_FORMAT,
                    minBufferSize,
                    AudioTrack.MODE_STREAM
                )
            }
            
            audioTrack?.play()
            audioTrack?.write(audioData, 0, audioData.size)
            
            // 等待播放完成
            delay((audioData.size * 1000L) / (SAMPLE_RATE * 2)) // 16bit = 2 bytes per sample
            
        } catch (e: Exception) {
            Log.e(TAG, "Playback error", e)
            throw e
        } finally {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
    }
    
    override fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
    
    override fun getAudioAmplitude(): Flow<Float> = flow {
        while (currentCoroutineContext().isActive) {
            if (_recordingState.value is RecordingState.Recording && audioRecord != null) {
                val buffer = ShortArray(1024)
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                
                if (readSize > 0) {
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        sum += abs(buffer[i].toFloat())
                    }
                    val amplitude = (sum / readSize) / Short.MAX_VALUE.toFloat()
                    emit(amplitude.coerceIn(0.0f, 1.0f))
                }
            } else {
                emit(0f)
            }
            delay(50) // 更新频率 20Hz
        }
    }.flowOn(Dispatchers.IO)
    
    override fun release() {
        stopRecording()
        stopPlayback()
        audioScope.cancel()
    }
}
