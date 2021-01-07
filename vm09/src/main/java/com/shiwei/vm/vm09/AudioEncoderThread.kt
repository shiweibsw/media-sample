package com.shiwei.vm.vm09

import android.content.Context
import java.lang.ref.WeakReference

/**
 * @Author shiwei
 * @Date 2021/1/5-16:58
 * @Email shiweibsw@gmail.com
 */
class AudioEncoderThread(muxer: WeakReference<MediaMuxerThread>) : Thread() {
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