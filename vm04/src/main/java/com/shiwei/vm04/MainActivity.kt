package com.shiwei.vm04

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var audioData: ByteArray
    private var audioTrack: AudioTrack? = null
    private var mTrackMinBufferSize = 0;

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initMinBufferSize()
        btnStart.setOnClickListener {
            releaseAudioTrack()
            audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, audioData.size, AudioTrack.MODE_STATIC)
            audioTrack?.write(audioData, 0, audioData.size)
            audioTrack?.play()
        }
        btnStart2.setOnClickListener {
            releaseAudioTrack()
            audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, mTrackMinBufferSize, AudioTrack.MODE_STREAM)
            Thread {
                var inputStream = resources.openRawResource(R.raw.vm03)
                try {
                    var tempBuffer = ByteArray(1024)
                    var readCount = 0;
                    while (inputStream.available() > 0) {
                        readCount = inputStream.read(tempBuffer)
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                            continue
                        }
                        if (readCount != 0 && readCount != -1) {
                            audioTrack?.play()
                            audioTrack?.write(tempBuffer, 0, readCount)
                        }
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
        loadAudioDatas()
    }

    private fun initMinBufferSize() {
        mTrackMinBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)
    }


    /**
     * STATIC 模式下需要先加载音频数据
     */
    private fun loadAudioDatas() {
        Thread {
            var inputStream = resources.openRawResource(R.raw.vm03)
            try {
                var outputStream = ByteArrayOutputStream(264848)
                var b = inputStream.read()
                while (b != -1) {
                    outputStream.write(b)
                    b = inputStream.read()
                }
                audioData = outputStream.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun releaseAudioTrack() {
        audioTrack?.stop()
        audioTrack?.release()
    }

    override fun onPause() {
        super.onPause()
        releaseAudioTrack()
    }
}