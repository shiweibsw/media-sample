package com.shiwei.vm.vm09

import java.lang.ref.WeakReference

/**
 * @Author shiwei
 * @Date 2021/1/5-16:59
 * @Email shiweibsw@gmail.com
 */
class VideoEncoderThread(
    width: Int,
    height: Int,
    var muxer: WeakReference<MediaMuxerThread>
) : Thread() {
    private val lock = Object()

    @Volatile
    private var isMuxerReady = false

    /**
     * MediaMuxer 是否准备完成
     */
    fun setMuxerReady(isMuxerReady: Boolean) {
        synchronized(lock) {
            this.isMuxerReady = isMuxerReady
            lock.notify()
        }
    }

    override fun run() {

    }
}