package com.shiwei.vm.vm09

import android.media.MediaCodec
import android.media.MediaMuxer
import android.util.Log
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*

/**
 * @Author shiwei
 * @Date 2021/1/5-16:49
 * @Email shiweibsw@gmail.com
 */
object MediaMuxerThread : Thread() {
    private const val TAG = "MediaMuxerThread"
    private var muxerDatas: Vector<MuxerData> = Vector()//使用Vector保证集合是线程安全的
    private var fileSwapHelper: FileUtils = FileUtils()
    private var audioThread: AudioEncoderThread? = null
    private var videoThread: VideoEncoderThread? = null
    private var mediaMuxer: MediaMuxer? = null
    private var width = 0
    private var height = 0

    // 开始音视频混合任务
    fun startMuxer(width: Int, height: Int) {
        this.width = width
        this.height = height
        run()
    }


    override fun run() {
        initMuxer()
    }

    /**
     * 初始化混合器
     */
    private fun initMuxer() {
        muxerDatas.clear()
        audioThread = AudioEncoderThread(WeakReference<MediaMuxerThread>(this))
        videoThread = VideoEncoderThread(width, height, WeakReference<MediaMuxerThread>(this))
        audioThread?.start()
        videoThread?.start()
        try {
            readyStart()
        } catch (e: IOException) {
            Log.i(TAG, "initMuxer: 异常")
        }
    }

    @Throws(IOException::class)
    private fun readyStart() {
        fileSwapHelper.requestSwapFile(true)
        readyStart(fileSwapHelper.nextFileName)
    }

    @Throws(IOException::class)
    private fun readyStart(filePath: String) {

    }

}

data class MuxerData(
    var trackIndex: Int,
    var byteBuffer: ByteBuffer,
    var bufferInfo: MediaCodec.BufferInfo
)