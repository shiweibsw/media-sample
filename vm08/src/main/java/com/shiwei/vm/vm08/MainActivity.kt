package com.shiwei.vm.vm08

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

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var videoFile = File(externalCacheDir, "test.h264")
        btnStart.setOnClickListener {
            mediaExtractor(videoFile)
        }
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