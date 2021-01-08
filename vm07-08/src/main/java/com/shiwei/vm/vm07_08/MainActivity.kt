package com.shiwei.vm.vm07_08

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
import java.io.FileOutputStream
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn1.setOnClickListener {
            Thread { extractorVideo() }.start()
        }
        btn2.setOnClickListener {
            Thread { muxerVideo() }.start()
        }
    }

    private fun extractorVideo() {
        var path =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "test.mp4"
        var file = File(path)
        if (!file.exists()) {
            Log.i(TAG, "未找到视频文件")
            return
        }
        var extractor = MediaExtractor()
        extractor.setDataSource(path)
        for (i in 0 until extractor.trackCount) {
            var format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)!!.startsWith("video/")) {
                Log.i(TAG, "视频通道：$i 开始分离")
                extractorAndMuxerVideo(extractor, i, "test.mp4")
                Log.i(TAG, "视频通道：分离完成")
                continue
            }
            if (format.getString(MediaFormat.KEY_MIME)!!.startsWith("audio/")) {
                Log.i(TAG, "音频通道：$i 开始分离")
                extractorAndMuxerAudio(extractor, i, "test.aac")
                Log.i(TAG, "音频通道：分离完成")
                continue
            }
        }
        extractor.release()
    }

    /**
     * 分离视频
     */
    private fun extractorAndMuxerVideo(
        extractor: MediaExtractor,
        trackId: Int,
        outputFileName: String
    ) {
        val format = extractor.getTrackFormat(trackId)
        var frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
        extractor.selectTrack(trackId)
        val outputFile = File(externalCacheDir, outputFileName)
        if (outputFile.exists())
            outputFile.delete()
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var muxerTrackId = muxer.addTrack(format)
        muxer.start()
        val maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        val byteBuffer = ByteBuffer.allocate(maxInputSize)
        val bufferInfo = MediaCodec.BufferInfo()
        var sampleSize = extractor.readSampleData(byteBuffer, 0)
        while (sampleSize > 0) {
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.flags = extractor.sampleFlags
            //因为mediaExtractor的提取顺序应该是dts的顺序不是pts的顺序，如果视频中存在b帧则getSampleTime不可能递增的，
            // 所以bufferInfo.presentationTimeUs=mediaExtractor.getSampleTime()可能会报错，前面说了这个值必须递增。如果不存在b帧，pts==dts，使用没问题。
            //这里通过帧率来计算
            bufferInfo.presentationTimeUs += 1000 * 1000 / frameRate
            muxer.writeSampleData(muxerTrackId, byteBuffer, bufferInfo)
            extractor.advance()
            sampleSize = extractor.readSampleData(byteBuffer, 0)
        }
        extractor.unselectTrack(trackId)
        muxer.stop()
        muxer.release()
    }

    /**
     * 分离音频
     */
    private fun extractorAndMuxerAudio(
        extractor: MediaExtractor,
        trackId: Int,
        outputFileName: String
    ) {
        val format = extractor.getTrackFormat(trackId)
        extractor.selectTrack(trackId)
        val outputFile = File(externalCacheDir, outputFileName)
        if (outputFile.exists())
            outputFile.delete()
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var muxerTrackId = muxer.addTrack(format)
        muxer.start()
        val maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        val byteBuffer = ByteBuffer.allocate(maxInputSize)
        val bufferInfo = MediaCodec.BufferInfo()
        var sampleSize = extractor.readSampleData(byteBuffer, 0)
        while (sampleSize > 0) {
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.flags = extractor.sampleFlags
            bufferInfo.presentationTimeUs = extractor.sampleTime
            muxer.writeSampleData(muxerTrackId, byteBuffer, bufferInfo)
            extractor.advance()
            sampleSize = extractor.readSampleData(byteBuffer, 0)
        }
        extractor.unselectTrack(trackId)
        muxer.stop()
        muxer.release()
    }


    private fun muxerVideo() {
        var videoFile = File(externalCacheDir, "test.mp4")
        var audioFile = File(externalCacheDir, "test.aac")
        var outputFile = File(externalCacheDir, "new_test.mp4")
        if (outputFile.exists()) {
            outputFile.delete()
        }
        if (!videoFile.exists()) {
            Log.i(TAG, "muxerVideo: 为找到视频文件")
            return
        }
        if (!audioFile.exists()) {
            Log.i(TAG, "muxerVideo: 为找到音频文件")
            return
        }
        var muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var videoExtractor: MediaExtractor = MediaExtractor()
        videoExtractor.setDataSource(videoFile.absolutePath)
        var videoTrackId = 0
        var videoMaxInputSize = 0
        var videoFrameRate = 0
        for (i in 0 until videoExtractor.trackCount) {
            var format = videoExtractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)!!.startsWith("video/")) {
                videoExtractor.selectTrack(i)
                videoTrackId = muxer.addTrack(format)
                videoMaxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                videoFrameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                break
            }
        }
        var audioExtractor: MediaExtractor = MediaExtractor()
        audioExtractor.setDataSource(audioFile.absolutePath)
        var audioTrackId = 0
        var audioMaxInputSize = 0
        for (i in 0 until audioExtractor.trackCount) {
            var format = audioExtractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)!!.startsWith("audio/")) {
                audioExtractor.selectTrack(i)
                audioTrackId = muxer.addTrack(format)
                audioMaxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                break
            }
        }
        muxer.start()

        var videoByteBuffer = ByteBuffer.allocate(videoMaxInputSize)
        var videoBufferInfo = MediaCodec.BufferInfo()
        var videoSampleSize = videoExtractor.readSampleData(videoByteBuffer, 0)
        while (videoSampleSize > 0) {
            videoBufferInfo.size = videoSampleSize
            videoBufferInfo.offset = 0
            videoBufferInfo.flags = videoExtractor.sampleFlags
            videoBufferInfo.presentationTimeUs += 1000 * 1000 / videoFrameRate
            muxer.writeSampleData(videoTrackId, videoByteBuffer, videoBufferInfo)
            videoExtractor.advance()
            videoSampleSize = videoExtractor.readSampleData(videoByteBuffer, 0)
        }

        var audioByteBuffer = ByteBuffer.allocate(audioMaxInputSize)
        var audioBufferInfo = MediaCodec.BufferInfo()
        var audioSampleSize = audioExtractor.readSampleData(audioByteBuffer, 0)
        while (audioSampleSize > 0) {
            audioBufferInfo.size = audioSampleSize
            audioBufferInfo.offset = 0
            audioBufferInfo.flags = audioExtractor.sampleFlags
            audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
            muxer.writeSampleData(audioTrackId, audioByteBuffer, audioBufferInfo)
            audioExtractor.advance()
            audioSampleSize = audioExtractor.readSampleData(audioByteBuffer, 0)
        }
        videoExtractor.release()
        audioExtractor.release()
        muxer.stop()
        muxer.release()
    }
}