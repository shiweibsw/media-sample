package com.shiwei.vm02

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * @Author shiwei
 * @Date 2020/12/29-14:53
 * @Email shiweibsw@gmail.com
 */
class CustomImageView : View {
    private var bitmap: Bitmap? = null
    private var paint = Paint()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    private fun init() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
    }


    fun drawBitmap(id: Int) {
        var option = BitmapFactory.Options()
        bitmap = BitmapFactory.decodeResource(resources, id, option)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas?.drawBitmap(it, 0f, 0f, paint)
        }
    }
}