package com.orion.ocrsampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import io.fotoapparat.Fotoapparat
import io.fotoapparat.log.fileLogger
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA")
    private val REQUEST_CODE_PERMISSIONS = 101
    private var fotoapparat: Fotoapparat? = null
    private var recognizer: TextRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fotoapparat = Fotoapparat(
                context = this,
                view = cameraView,                   // view which will draw the camera preview
                scaleType = ScaleType.CenterCrop,    // (optional) we want the preview to fill the view
                lensPosition = back(),               // (optional) we want back camera
                logger = loggers(                    // (optional) we want to log camera events in 2 places at once
                        logcat(),                   // ... in logcat
                        fileLogger(this)            // ... and to file
                ),
                cameraErrorCallback = { error -> }   // (optional) log fatal errors
        )

        recognizer = TextRecognition.getClient()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            return
        } else {

        }

        camera_capture_button.setOnClickListener {
            val photoResult = fotoapparat?.takePicture()
            photoResult?.toBitmap()!!.whenAvailable {
                Toast.makeText(applicationContext, "Captured Image", Toast.LENGTH_LONG).show()
                val result = recognizer?.process(InputImage.fromBitmap(it!!.bitmap, 0))!!
                        .addOnSuccessListener { visionText ->
                            val resultText = visionText.text
                            Toast.makeText(applicationContext,""+resultText,Toast.LENGTH_LONG).show()
                            for (block in visionText.textBlocks) {
                                val blockText = block.text
                                val blockCornerPoints = block.cornerPoints
                                val blockFrame = block.boundingBox
                                for (line in block.lines) {
                                    val lineText = line.text
                                    val lineCornerPoints = line.cornerPoints
                                    val lineFrame = line.boundingBox
                                    for (element in line.elements) {
                                        val elementText = element.text
                                        val elementCornerPoints = element.cornerPoints
                                        val elementFrame = element.boundingBox
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            // Task failed with an exception
                            // ...
                        }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        fotoapparat?.start()
    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
    }


}

