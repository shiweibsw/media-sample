package com.shiwei.vm05

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private val TAG = "MainActivity"
    private var camera: Camera? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var currentCameraId = CameraInfo.CAMERA_FACING_BACK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "使用SurfaceView 预览Camera"
        mSurfaceHolder = surfaceView.holder
        mSurfaceHolder?.addCallback(this)
        btnBack.setOnClickListener {
            currentCameraId = CameraInfo.CAMERA_FACING_BACK
            switchCamera()
        }
        btnFront.setOnClickListener {
            currentCameraId = CameraInfo.CAMERA_FACING_FRONT
            switchCamera()
        }
        //配置获取NV21格式的数据
//        var parameters = camera.parameters
//        parameters.previewFormat = ImageFormat.NV21
//        camera.parameters = parameters
//        camera.setPreviewCallback { data, camera ->
//            Log.i("TAG", "onCreate: ${Arrays.toString(data)}")
//        }
    }

    private fun switchCamera() {
        releaseCamera()
        initCamera()
        try {
            camera?.setPreviewDisplay(mSurfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        startPreview()
    }

    private fun initCamera() {
        camera = Camera.open(currentCameraId)
        camera?.setDisplayOrientation(90)
    }

    private fun releaseCamera() {
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun startPreview() {
        camera?.setOneShotPreviewCallback { data, camera ->
            var previewSize = camera?.parameters?.previewSize
            var yuvImage =
                YuvImage(data, ImageFormat.NV21, previewSize!!.width, previewSize!!.height, null)
            var os = ByteArrayOutputStream(data.size)
            if (!yuvImage.compressToJpeg(
                    Rect(0, 0, previewSize!!.width, previewSize!!.height),
                    100,
                    os
                )
            ) {
                return@setOneShotPreviewCallback
            }
            var bytes = os.toByteArray()
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            pivPre.setImageBitmap(bitmap)
        }
        camera?.startPreview()
    }

    private fun surfaceCreated() {
        if (mSurfaceHolder == null) {
            mSurfaceHolder = surfaceView.holder
        }
        if (camera == null) {
            initCamera()
        }
        try {
            camera?.setPreviewDisplay(mSurfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        startPreview()
    }

    private fun surfaceDestroyed() {
        releaseCamera()
        mSurfaceHolder = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.i(TAG, "surfaceChanged: ")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceDestroyed: ")
        surfaceDestroyed()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i(TAG, "surfaceCreated: ")
        surfaceCreated()
    }
}