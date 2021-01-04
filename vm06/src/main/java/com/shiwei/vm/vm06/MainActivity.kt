package com.shiwei.vm.vm06

import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * 使用MediaExtractor 获取媒体文件的基本信息
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var file = File(Environment.getExternalStorageDirectory().absolutePath + "/test.mp4")
        btnGetMime.setOnClickListener {
            getTrackByIndex(file, 0)
        }
    }

    private fun getTrackByIndex(file: File, index: Int): Int {
        var destTrackIndex = -1;
        try {
            var extractor = MediaExtractor()
            extractor.setDataSource(file.absolutePath)
            var mediaFormat = extractor.getTrackFormat(index)
            Log.i(TAG, "语言格式:${mediaFormat.getString(MediaFormat.KEY_LANGUAGE)} ")
            Log.i(TAG, "视频宽度:${mediaFormat.getInteger(MediaFormat.KEY_WIDTH)} ")
            Log.i(TAG, "视频高度:${mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)} ")
            Log.i(TAG, "总时长:${mediaFormat.getLong(MediaFormat.KEY_DURATION)} ")
            Log.i(TAG, "最大缓冲区:${mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)} ")
            Log.i(TAG, "以下是不确定可以获取到的信息")
            Log.i(TAG, "采样率:${mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)} ")
            Log.i(TAG, "比特率:${mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE)} ")
            Log.i(TAG, "声道数量:${mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)} ")
            Log.i(TAG, "最大宽度:${mediaFormat.getInteger(MediaFormat.KEY_MAX_WIDTH)} ")
            Log.i(TAG, "最大高度:${mediaFormat.getInteger(MediaFormat.KEY_MAX_HEIGHT)} ")
            Log.i(TAG, "颜色格式:${mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT)} ")
            Log.i(TAG, "帧率:${mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)} ")
            Log.i(TAG, "图块宽度:${mediaFormat.getInteger(MediaFormat.KEY_TILE_WIDTH)} ")
            Log.i(TAG, "图块高度:${mediaFormat.getInteger(MediaFormat.KEY_TILE_HEIGHT)} ")
            Log.i(TAG, "网格行:${mediaFormat.getInteger(MediaFormat.KEY_GRID_ROWS)} ")
            Log.i(TAG, "网格列:${mediaFormat.getInteger(MediaFormat.KEY_GRID_COLUMNS)} ")
            Log.i(TAG, "PCM-编码:${mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)} ")
            Log.i(TAG, "捕获率:${mediaFormat.getFloat(MediaFormat.KEY_CAPTURE_RATE)} ")
            Log.i(TAG, "是否AAS:${mediaFormat.getInteger(MediaFormat.KEY_IS_ADTS)} ")
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return destTrackIndex
    }

    /**
     * 获取指定通道
     */
    private fun getTrackByIndex(file: File, trackName: String): Int {
        var destTrackIndex = -1;
        try {
            var extractor = MediaExtractor()
            extractor.setDataSource(file.absolutePath)
            for (i in 0 until extractor.trackCount) {
                var mediaFormat = extractor.getTrackFormat(i)
                var mimeFormat = mediaFormat.getString(MediaFormat.KEY_MIME)
                if (mimeFormat!!.startsWith(trackName)) {
                    destTrackIndex = i
                    break
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return destTrackIndex
    }

    /**
     * 获取当前媒体文件所有通道的MIME类型
     */
    private fun mediaExtractor(file: File) {
        try {
            var extractor = MediaExtractor()
            extractor.setDataSource(file.absolutePath)
            for (i in 0 until extractor.trackCount) {
                var mediaFormat = extractor.getTrackFormat(i)
                var mimeFormat = mediaFormat.getString(MediaFormat.KEY_MIME)
                Log.i(TAG, "mediaExtractor:$mimeFormat ")
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}