package me.ahmednobi.qrcode_scanner

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executors


private const val  REQUESTED_CODE =10
private val requestedPermission = arrayOf(android.Manifest.permission.CAMERA)
private val executor = Executors.newSingleThreadExecutor()

    class MainActivity : AppCompatActivity() {
        companion object {

            /** Convenience method used to check if all permissions required by this app are granted */
            fun hasPermissions(context: Context) = requestedPermission .all {

                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!hasPermissions(this)) {
            // Request camera-related permissions
            requestPermissions(requestedPermission, REQUESTED_CODE)
        }else {
            textureView.post { startCamera() }
        }
textureView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
    updateTransform()
}
    }
        override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == REQUESTED_CODE) {
                if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                    // Take the user to the success fragment when permission is granted
                    Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
textureView.post { startCamera() }
                } else {
                    Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
                }
            }
        }

private fun startCamera() {
    val previewConfig = PreviewConfig.Builder().apply {
        setTargetResolution(Size(640,480))
    }.build()

val preview = Preview(previewConfig)
  preview.setOnPreviewOutputUpdateListener{

      val parent = textureView.parent as ViewGroup

      parent.removeView(textureView)
      parent.addView(textureView , 0)
      textureView.surfaceTexture = it.surfaceTexture
  updateTransform()
  }
    val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
        setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
    }.build()
    val imageCapture = ImageCapture(imageCaptureConfig)
    button.setOnClickListener {
        val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
    imageCapture.flashMode = FlashMode.ON

        imageCapture.takePicture(file , executor , object  :ImageCapture.OnImageSavedListener {
            override fun onImageSaved(file: File) {
                val msg = "Photo capture succeeded: ${file.absolutePath}"
                Log.d("CameraXApp", msg)
                textureView.post {
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(
                imageCaptureError: ImageCapture.ImageCaptureError,
                message: String,
                cause: Throwable?
            ) {
val msg = "Phot capture failed :$message"
                Log.e("CameraX" , msg , cause)
                textureView.post { Toast.makeText(baseContext , msg , Toast.LENGTH_SHORT).show() }
                     }

        })

    }
CameraX.bindToLifecycle(this , preview ,imageCapture)
}

        private fun updateTransform() {
val matrix = Matrix()
            val  centerX = textureView.width/2f
            val centerY  = textureView.height/2f
            val rotationDegrees = when(textureView.display.rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> return
            }
            matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

            // Finally, apply transformations to our TextureView
            textureView.setTransform(matrix)

        }

    }

