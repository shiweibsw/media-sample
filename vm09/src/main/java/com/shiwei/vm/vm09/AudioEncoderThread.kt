package com.shiwei.vm.vm09

import java.lang.ref.WeakReference
import java.nio.ByteBuffer

/**
 * @Author shiwei
 * @Date 2021/1/5-16:58
 * @Email shiweibsw@gmail.com
 */
class AudioEncoderThread(muxer: WeakReference<MediaMuxerThread>) : Thread() {
    private val lock = Object()
    val SAMPLES_PER_FRAME = 1024

    @Volatile
    private var isMuxerReady = false

    @Volatile
    private var isFinished = false

    @Volatile
    private var isStart = false

    /**
     * MediaMuxer 是否准备完成
     */
    fun setMuxerReady(isMuxerReady: Boolean) {
        synchronized(lock) {
            this.isMuxerReady = isMuxerReady
            lock.notify()
        }
    }

    fun finish() {
        isFinished = true
    }

    private fun startMediaCodec() {

    }

    private fun stopMediaCodec() {

    }


    override fun run() {
        var byteBuffer = ByteBuffer.allocate(SAMPLES_PER_FRAME)
        while (!isFinished) {
            /*启动或者重启*/
            if (!isStart) {
                stopMediaCodec()
                if (!isMuxerReady) {
                    synchronized(lock) {
                        lock.wait()
                    }
                } else {
                    startMediaCodec()
                }
            } else {
                byteBuffer.clear()
            }

        }
    }
}