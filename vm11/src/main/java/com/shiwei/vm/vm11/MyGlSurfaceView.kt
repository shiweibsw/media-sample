package com.shiwei.vm.vm11

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @Author shiwei
 * @Date 2021/1/8-14:05
 * @Email shiweibsw@gmail.com
 */
class MyGlSurfaceView : GLSurfaceView {
    private val TAG = "MyGlSurfaceView"

    private lateinit var mRender: MyGlRender

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        setEGLContextClientVersion(2)
        mRender = MyGlRender()
        setRenderer(mRender)
    }

    inner class MyGlRender : GLSurfaceView.Renderer {
        /**
         * 每一次View的重绘都会调用
         */
        override fun onDrawFrame(gl: GL10?) {
            Log.i(TAG, "onDrawFrame: ")
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }

        /**
         * 如果视图的几何形状发生变化（例如，当设备的屏幕方向改变时），则调用此方法。
         */
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Log.i(TAG, "onSurfaceChanged: ")
            GLES20.glViewport(0, 0, width, height)
        }

        /**
         * 在View的OpenGL环境被创建的时候调用。
         */
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.i(TAG, "onSurfaceCreated: ")
            GLES20.glClearColor(0f, 0f, 0f, 1f)
        }
    }
}