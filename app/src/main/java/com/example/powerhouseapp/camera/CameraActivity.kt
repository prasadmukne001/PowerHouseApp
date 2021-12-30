package com.example.powerhouseapp.camera

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import com.example.powerhouseapp.R
import com.example.powerhouseapp.databinding.ActivityCameraBinding
import com.example.powerhouseapp.google_ar.GoogleArActivity
import com.example.powerhouseapp.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraActivity"
private const val DELAYED_TIMER = 1000L

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    private val viewModel by viewModels<CameraViewModel>()
    private var isFirstPicTaken = false
    private var isSecondPicTaken = false
    private lateinit var mBinding: ActivityCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private val mDelayedCallRedirectionHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Snackbar.make(
                    mBinding.root,
                    getString(R.string.req_camera_permission),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initUI()

        registerImageUploadObserver()
    }

    private fun initUI() {

        setupCamera()

        mBinding.cameraCaptureButton.setOnClickListener {

            takePhoto()
            animateFlash()

            if (!isFirstPicTaken) {
                isFirstPicTaken = true
                Toast.makeText(
                    this,
                    getString(R.string.first_image_taken),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (isFirstPicTaken && !isSecondPicTaken) {
                isSecondPicTaken = true
                Toast.makeText(
                    this,
                    getString(R.string.second_image_taken),
                    Toast.LENGTH_LONG
                ).show()
                mDelayedCallRedirectionHandler.postDelayed({
                    redirectGoogleToARScreen()
                    finish()
                }, DELAYED_TIMER)
            }
        }
    }

    private fun setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()
        cameraPermissionResult.launch(android.Manifest.permission.CAMERA)
    }

    private fun registerImageUploadObserver() {
        viewModel.apiManager.imageUploadResponse.observe(this, Observer {
            Log.d(TAG, "$it")
        })
    }

    private fun animateFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBinding.root.postDelayed({
                mBinding.root.foreground = ColorDrawable(Color.WHITE)
                mBinding.root.postDelayed({
                    mBinding.root.foreground = null
                }, 50)
            }, 100)
        }
    }

    private fun redirectGoogleToARScreen() {
        val intent = Intent(this@CameraActivity, GoogleArActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startCamera() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(mBinding.viewFinder.surfaceProvider)
        }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        imageCapture?.let {
            val fileName = "Image_${System.currentTimeMillis()}.jpg"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i(TAG, "The image has been saved in ${file.toUri()}")
                        if (isFirstPicTaken && !isSecondPicTaken) {
                            viewModel.apiManager.uploadImage(
                                Constants.EMAIL_ID,
                                file, false
                            )
                            runOnUiThread {
                                Toast.makeText(
                                    this@CameraActivity,
                                    getString(R.string.first_image_upload_started),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (isSecondPicTaken) {
                            viewModel.apiManager.uploadImage(
                                Constants.EMAIL_ID,
                                file, false
                            )
                            runOnUiThread {
                                Toast.makeText(
                                    this@CameraActivity,
                                    getString(R.string.second_image_upload_started),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            this@CameraActivity,
                            getString(R.string.error_photo_capture),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("TAG", "Error taking photo:$exception")
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDelayedCallRedirectionHandler.removeCallbacksAndMessages(null)
    }
}