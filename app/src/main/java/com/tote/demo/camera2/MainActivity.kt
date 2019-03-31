package com.tote.demo.camera2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        camera_texture_view.surfaceTextureListener = object : SurfaceTextureListener {

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                Log.d(TAG, "SURFACE TEXTURE SIZE CHANGED")
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                camera_texture_view.bitmap.toGrayScale()
                Log.d(TAG, "SURFACE TEXTURE UPDATE")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                Log.d(TAG, "SURFACE TEXTURE SIZE DESTROYED")
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                openCamera(width, height)
                Log.d(TAG, "SURFACE TEXTURE IS AVAILABLE$width and $height")
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.first()
        cameraManager.openCamera(cameraId, State(camera_texture_view, width, height), null)
    }

    class State(private val textureView: TextureView, private val width: Int, private val height: Int) :
        CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice) {

            val surfaceTexture = textureView.surfaceTexture
            surfaceTexture.setDefaultBufferSize(width, height)
            val surface = Surface(surfaceTexture)
            val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequest.addTarget(surface)

            camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.d(TAG, "CAMERA CONFIGURATION IS FAILED")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    session.setRepeatingRequest(captureRequest.build(), null, null)
                    Log.d(TAG, "CAMERA CONFIGURATION IS SUCCESS")
                }

            }, null)

            Log.d(TAG, "CAMERA DEVICE IS OPENED")
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            Log.d(TAG, "CAMERA DEVICE IS DISCONNECTED")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            Log.d(TAG, "CAMERA DEVICE HAS ERRORS")
        }

    }

    fun Bitmap.toGrayScale(): Bitmap {
        val bitmapGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapGrayScale)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorMatrixColorFilter
        canvas.drawBitmap(this, 0f, 0f, paint)
        return bitmapGrayScale;
    }

}
