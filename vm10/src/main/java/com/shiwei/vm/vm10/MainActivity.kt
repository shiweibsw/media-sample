package com.shiwei.vm.vm10

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnStart.setOnClickListener {
            Thread {
                aacToPcm()
            }.start()
        }
    }

    private fun aacToPcm() {
        var outputFile = File(externalCacheDir, "aacToPcm.pcm")
        if (outputFile.exists())
            outputFile.delete()
        var inputFile = File(externalCacheDir, "test.aac")
        if (!inputFile.exists()) {
            Log.i(TAG, "aacToPcm: 未找到文件")
            return
        }
        var fileOutputStream = FileOutputStream(outputFile.absolutePath)

        initMediaCodec(inputFile, fileOutputStream)
    }

    private fun initMediaCodec(inputFile: File, fileOutputStream: FileOutputStream) {
        var mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(inputFile.absolutePath)
        var mediaMime = ""
        var format: MediaFormat? = null
        for (i in 0 until mediaExtractor.trackCount) {
            format = mediaExtractor.getTrackFormat(i)
            mediaMime = format.getString(MediaFormat.KEY_MIME)!!
            if (mediaMime.startsWith("audio/")) {
                mediaExtractor.selectTrack(i)
                break
            }
        }
        //生成MediaCodec，此时处于Uninitialized状态
        var codec = MediaCodec.createDecoderByType(mediaMime)
        //configure 处于Configured状态
        codec.configure(format, null, null, 0)
        //异步方式
        codec.setCallback(object : MediaCodec.Callback() {
            //1. 从codecInputBuffer中拿到empty input buffer的index
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                Log.i(TAG, "onInputBufferAvailable: $index")
                if (index >= 0) {
                    //2. 通过index获取到inputBuffer
                    var inputBuffer = codec.getInputBuffer(index)
                    inputBuffer?.let {
                        it.clear()
                        var sampleSize = mediaExtractor.readSampleData(it, 0)
                        //3. 如果读取不到数据，则认为是EOS。把数据生产端的buffer 送回到code的inputbuffer
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(
                                index,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        } else {
                            codec.queueInputBuffer(
                                index,
                                0,
                                sampleSize,
                                mediaExtractor.sampleTime,
                                0
                            )
                        }
                        //读取下一帧
                        mediaExtractor.advance()
                    }
                }
            }

            //4. 数据消费端Client 拿到一个有数据的outputbuffer的index
            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                Log.i(TAG, "onOutputBufferAvailable: $index")
                if (index >= 0) {
                    //5. 通过index获取到inputBuffer
                    var outputBuffer = codec.getOutputBuffer(index)
                    outputBuffer?.let {
                        var bytes = ByteArray(info.size)
                        it.get(bytes)
                        fileOutputStream.write(bytes)
                        fileOutputStream.flush()
                    }
                    //6. 然后清空outputbuffer，再释放给codec的outputbuffer
                    codec.releaseOutputBuffer(index, false)
                    //结束，释放资源
                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        mediaExtractor.release()
                        codec.stop()
                        codec.release()
                        fileOutputStream.close()
                    }
                }

            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Log.i(TAG, "onOutputFormatChanged: ")
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Log.i(TAG, "onError: ")
            }
        })
        codec.start()


    }


}