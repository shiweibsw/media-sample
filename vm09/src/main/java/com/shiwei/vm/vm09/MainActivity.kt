package com.shiwei.vm.vm09

import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity(), Camera.PreviewCallback, SurfaceHolder.Callback {
    private val TAG = "MainActivity"
    private lateinit var camera: Camera
    private lateinit var surfaceHolder: SurfaceHolder
    private var width = 1280
    private var height = 720
    private var isRecording = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        btnStart.setOnClickListener {
            if (!isRecording) {//开始
                MediaMuxerThread.startMuxer()
            } else {//结束

            }
            isRecording = !isRecording
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.i(TAG, "surfaceChanged: ")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceDestroyed: ")
        releaseCamera()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceCreated: ")
        initCamera(holder)
    }

    /**
     * 初始化相机并开始预览
     */
    private fun initCamera(holder: SurfaceHolder) {
        camera = Camera.open()
        camera.setDisplayOrientation(90)
        var paras = camera.parameters
        paras.previewFormat = ImageFormat.NV21
        paras.setPreviewSize(width, height)
        try {
            camera.parameters = paras
            camera.setPreviewDisplay(holder)
            camera.setPreviewCallback(this)
            camera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 停止相机预览
     */
    private fun releaseCamera() {
        camera.setPreviewCallback(null)
        camera.stopPreview()
    }
}