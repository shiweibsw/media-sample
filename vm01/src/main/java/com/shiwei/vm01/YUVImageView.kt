package com.shiwei.vm01

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

/**
 * TODO: document your custom view class.
 */
class YUVImageView : SurfaceView {
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap
    private lateinit var options: BitmapFactory.Options
    private lateinit var paint: Paint
    private lateinit var srcRect: Rect
    private lateinit var destRect: Rect
    private var callback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
        }

    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        surfaceHolder = holder
        surfaceHolder.setFixedSize(640, 480)
        surfaceHolder.addCallback(callback)
        setZOrderOnTop(true)
        setZOrderMediaOverlay(true)//不会遮挡上面的控件
        paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        srcRect = Rect(0, 0, 640, 480)
        destRect = Rect(0, 0, 640, 480)
        options = BitmapFactory.Options()
    }

    fun drawBitmap(id: Int) {
        canvas = surfaceHolder.lockCanvas()
        canvas.drawColor(Color.BLUE)
        bitmap = BitmapFactory.decodeResource(resources, id, options)
//        var matrix=Matrix()
//        matrix.setScale((640/options.outWidth).toFloat(),(480/options.outHeight).toFloat())
//        canvas.drawBitmap(bitmap,matrix,paint)
        canvas.drawBitmap(bitmap, srcRect, destRect, paint)
        surfaceHolder.unlockCanvasAndPost(canvas)
    }
}