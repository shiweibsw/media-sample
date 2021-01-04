package com.shiwei.vm.vm08

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer

/**
 * 使用MediaMuxer合成视频
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var videoFile = File(Environment.getExternalStorageDirectory().absolutePath, "test.mp4")
        btnStart.setOnClickListener {
            Thread {
                mediaMuxer(videoFile)
            }.start()
        }
    }

    /**
     *  从MP4文件中提取视频并生成新的视频文件
     */
    private fun mediaMuxer(file: File) {
        try {
            Log.i(TAG, "mediaMuxer: 开始")
            var extractor = MediaExtractor()
            var mediaMuxer: MediaMuxer? = null
            var framerate = 0
            var mVideoTrackIndex = 0;
            extractor.setDataSource(file.absolutePath)
            for (i in 0 until extractor.trackCount) {
                var mediaFormat = extractor.getTrackFormat(i)
                if (!mediaFormat.getString(MediaFormat.KEY_MIME)!!.startsWith("video/")) {
                    continue
                }
                framerate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
                extractor.selectTrack(i)
                mediaMuxer = MediaMuxer(
                    externalCacheDir?.absolutePath + "output.mp4",
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )
                mVideoTrackIndex = mediaMuxer.addTrack(mediaFormat)
                mediaMuxer.start()
                break
            }
            mediaMuxer?.let {
                var info = MediaCodec.BufferInfo()
                info.presentationTimeUs = 0
                var buffer = ByteBuffer.allocate(500 * 1024)
                var sampleSize = extractor.readSampleData(buffer, 0)
                while (sampleSize > 0) {
                    info.offset = 0
                    info.size = sampleSize
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
                    info.presentationTimeUs += 1000 * 1000 / framerate
                    it.writeSampleData(mVideoTrackIndex, buffer, info)
                    extractor.advance()
                    sampleSize = extractor.readSampleData(buffer, 0)
                }
                extractor.release()
                it.stop()
                it.release()
            }

            Log.i(TAG, "mediaMuxer: 结束")
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}