package com.shiwei.vm05

import android.graphics.SurfaceTexture
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.IOException

class MainActivity2 : AppCompatActivity(), TextureView.SurfaceTextureListener {
    private lateinit var camera: Camera
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        title = "使用TextureView 预览Camera"
        textureView.surfaceTextureListener = this
        camera = Camera.open()
        camera.setDisplayOrientation(90)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        camera.release()
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        try {
            camera.setPreviewTexture(surface)
            camera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}