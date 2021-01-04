package com.shiwei.vm05

import android.graphics.ImageFormat
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var camera: Camera
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "使用SurfaceView 预览Camera"
        surfaceView.holder.addCallback(this)
        camera = Camera.open()
        camera.setDisplayOrientation(90)

        //配置获取NV21格式的数据
        var parameters = camera.parameters
        parameters.previewFormat = ImageFormat.NV21
        camera.parameters = parameters
        camera.setPreviewCallback { data, camera ->
            Log.i("TAG", "onCreate: ${Arrays.toString(data)}")
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}