package com.shiwei.vm.vm07

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * 使用MediaExtractor分离视频
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var file = File(Environment.getExternalStorageDirectory().absolutePath + "/test.mp4")
        btnSeparate.setOnClickListener {
            Thread {
                Log.i(TAG, "onCreate: 开始分离")
                separate(file)
                Log.i(TAG, "onCreate: 分离完成")
            }.start()
        }
    }

    private fun separate(file: File) {
        var mediaExtractor = MediaExtractor()
        try {
            mediaExtractor.setDataSource(file.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var trackCount = mediaExtractor.trackCount//获取通道数
        var videoTrack = 0//视频通道
        var videoMediaFormat: MediaFormat? = null//视频格式
        var audioTrack = 0;//音频通道
        var audioMediaFormat: MediaFormat? = null//音频格式

        for (i in 0 until trackCount) {//查找需要的视频轨道与音频轨道index
            var mediaFormat = mediaExtractor.getTrackFormat(i)
            var mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video")) {
                videoTrack = i;
                videoMediaFormat = mediaFormat
                continue
            }
            if (mime!!.startsWith("audio")) {
                audioTrack = i;
                audioMediaFormat = mediaFormat
                continue
            }
        }

        var videoFile = File(externalCacheDir, "test.h264")
        var audioFile = File(externalCacheDir, "audio.acc")
        if (videoFile.exists())
            videoFile.delete()
        if (audioFile.exists())
            audioFile.delete()
        //开始分离
        try {
            var videoOutputStream = FileOutputStream(videoFile)
            var audioOutputStream = FileOutputStream(audioFile)

            /**
             * 分离视频
             */
            var maxVideoBufferCount =
                videoMediaFormat?.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)//获取视频的输出缓存的最大大小
            var videoBuffer = ByteBuffer.allocate(maxVideoBufferCount!!)
            mediaExtractor.selectTrack(videoTrack)
            var len = mediaExtractor.readSampleData(videoBuffer, 0)
            while (len != -1) {
                var bytes = ByteArray(len)
                videoBuffer.get(bytes)
                videoOutputStream.write(bytes)
                videoBuffer.clear()
                mediaExtractor.advance()//读取下一帧
                len = mediaExtractor.readSampleData(videoBuffer, 0)
            }
            videoOutputStream.flush()
            videoOutputStream.close()
            mediaExtractor.unselectTrack(videoTrack)//取消选择视频轨道


            /**
             * 分离音频
             */
            var maxAudioBufferCount = audioMediaFormat?.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            var audioBuffer = ByteBuffer.allocate(maxAudioBufferCount!!)
            mediaExtractor.selectTrack(audioTrack)
            var len1 = mediaExtractor.readSampleData(audioBuffer, 0)
            while (len1 != -1) {
                var bytes = ByteArray(len1)
                audioBuffer.get(bytes)

                /**
                 * 添加adts头
                 */
                var adts = ByteArray(len1 + 7)
                addADTStoPacket(adts, len1 + 7)
                System.arraycopy(bytes, 0, adts, 7, len1)

                audioOutputStream.write(bytes)
                audioBuffer.clear()
                mediaExtractor.advance()
                len1 = mediaExtractor.readSampleData(audioBuffer, 0)
            }
            audioOutputStream.flush()
            audioOutputStream.close()
            mediaExtractor.unselectTrack(audioTrack)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaExtractor.release()//释放资源
    }

    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        /*
        标识使用AAC级别 当前选择的是LC
        一共有1: AAC Main 2:AAC LC (Low Complexity) 3:AAC SSR (Scalable Sample Rate) 4:AAC LTP (Long Term Prediction)
        */
        val profile = 2
        val frequencyIndex = 0x04 //设置采样率
        val channelConfiguration = 2 //设置频道,其实就是声道
        // fill in ADTS data
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] =
            ((profile - 1 shl 6) + (frequencyIndex shl 2) + (channelConfiguration shr 2)).toByte()
        packet[3] = ((channelConfiguration and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }
}