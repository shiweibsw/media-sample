package com.shiwei.vm01

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sfView.postDelayed({
            sfView.drawBitmap(R.mipmap.ic_launcher)
        }, 200)

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.i(TAG, "surfaceChanged: ")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.i(TAG, "surfaceDestroyed: ")
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.i(TAG, "surfaceCreated: ")
                var canvas = holder.lockCanvas()
                var paint = Paint()
                var bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
                holder.unlockCanvasAndPost(canvas)
            }
        })
    }
}