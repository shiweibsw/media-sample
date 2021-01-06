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
) :
    Thread() {

    override fun run() {

    }
}