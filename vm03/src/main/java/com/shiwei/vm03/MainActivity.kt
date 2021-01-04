package com.shiwei.vm03

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var mAudioRecord: AudioRecord
    private var mRecordBufferSize = 0
    private var pcmFile: File? = null
    private var wavFile: File? = null
    private var isRecording = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initMinBufferSize()
        initAudioRecord()
        btnStartRecord.setOnClickListener {
            startRecord()
        }
        btnStopRecord.setOnClickListener {
            stopRecord()
        }
    }

    /**
     * 初始化每一帧的流大小
     */
    private fun initMinBufferSize() {
        mRecordBufferSize = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
    }

    /**
     * 初始化音频录制
     */
    private fun initAudioRecord() {
        mAudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                mRecordBufferSize)
    }

    /**
     * 开始录制
     */
    private fun startRecord() {
        pcmFile = File(externalCacheDir?.path, "vm03.pcm")
        isRecording = true
        Thread {
            mAudioRecord.startRecording()//开始录制
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(pcmFile)
                var bytes = ByteArray(mRecordBufferSize)
                while (isRecording) {
                    mAudioRecord.read(bytes, 0, bytes.size)
                    fileOutputStream?.write(bytes)
                    fileOutputStream.flush()
                }
                mAudioRecord.stop()
                fileOutputStream.flush()
                fileOutputStream.close()
                addHeadData()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                mAudioRecord.stop()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 停止录制
     */
    private fun stopRecord() {
        isRecording = false
    }

    /**
     * 添加头部信息
     */
    private fun addHeadData() {
        pcmFile = File(externalCacheDir?.path, "vm03.pcm")
        wavFile = File(externalCacheDir?.path, "vm03.wav")
        var pcmToWavUtil = PcmToWavUtil(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        pcmToWavUtil.pcmToWav(pcmFile.toString(), wavFile.toString())
    }

    /**
     * 释放资源
     */
    override fun onDestroy() {
        super.onDestroy()
        mAudioRecord.release()
    }


}