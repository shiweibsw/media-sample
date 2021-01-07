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
    const val TRACK_VIDEO = 0
    const val TRACK_AUDIO = 1
    private var muxerDatas: Vector<MuxerData> = Vector()//使用Vector保证集合是线程安全的
    private var fileSwapHelper: FileUtils = FileUtils()
    private var audioThread: AudioEncoderThread? = null
    private var videoThread: VideoEncoderThread? = null
    private var mediaMuxer: MediaMuxer? = null
    private var width = 0
    private var height = 0
    private var lock = Object();

    @Volatile
    private var isFinished = false

    @Volatile
    private var isVideoTrackAdded = false

    @Volatile
    private var isAudioTrackAdded = false

    // 开始音视频混合任务
    fun startMuxer(width: Int, height: Int) {
        this.width = width
        this.height = height
        run()
    }

    /**
     * 混合器是否正在运行
     */
    fun isMuxerRunning(): Boolean = isAudioTrackAdded && isVideoTrackAdded


    override fun run() {
        initMuxer()
        while (!isFinished) {
            if (isMuxerRunning() || muxerDatas.isEmpty()) {
                if (fileSwapHelper.requestSwapFile()) {//是否需要切换存储路径
                    readyStart(fileSwapHelper.nextFileName)
                } else {
                    var data = muxerDatas.removeAt(0)//removeAt 返回的是移除的对象
                    var track=0
                    if (data.trackIndex== TRACK_VIDEO){

                    }
                }

            } else {
                //等待音视频线程初始化或者等待数据
                synchronized(lock) {
                    try {
                        lock.wait()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
//        readyStop()
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
        isFinished = false
        isAudioTrackAdded = false
        isVideoTrackAdded = false
        muxerDatas.clear()
        mediaMuxer = MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        audioThread?.let { it.setMuxerReady(true) }
        videoThread?.let { it.setMuxerReady(true) }
    }

}

data class MuxerData(
    var trackIndex: Int,
    var byteBuffer: ByteBuffer,
    var bufferInfo: MediaCodec.BufferInfo
)